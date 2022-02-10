package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.events.model.ShipmentEvent;
import org.dcsa.core.events.model.enums.DocumentTypeCode;
import org.dcsa.core.events.model.enums.EventClassifierCode;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.core.events.model.transferobjects.CargoItemTO;
import org.dcsa.core.events.model.transferobjects.ShipmentEquipmentTO;
import org.dcsa.core.events.repository.ShipmentRepository;
import org.dcsa.core.events.service.*;
import org.dcsa.core.exception.ConcreteRequestErrorMessageException;
import org.dcsa.ebl.model.ShippingInstruction;
import org.dcsa.ebl.model.mappers.ShippingInstructionMapper;
import org.dcsa.ebl.model.transferobjects.ShippingInstructionResponseTO;
import org.dcsa.ebl.model.transferobjects.ShippingInstructionTO;
import org.dcsa.ebl.repository.ShippingInstructionRepository;
import org.dcsa.ebl.service.ShippingInstructionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ShippingInstructionServiceImpl implements ShippingInstructionService {

  private final ShippingInstructionRepository shippingInstructionRepository;
  private final LocationService locationService;
  private final ShipmentEquipmentService shipmentEquipmentService;
  private final ShipmentEventService shipmentEventService;
  private final DocumentPartyService documentPartyService;
  private final ReferenceService referenceService;

  // Repositories
  private final ShipmentRepository shipmentRepository;

  // Mappers
  private final ShippingInstructionMapper shippingInstructionMapper;

  @Override
  public Mono<ShippingInstructionTO> findById(String shippingInstructionID) {
    return null;
  }

  @Transactional
  @Override
  public Mono<ShippingInstructionResponseTO> createShippingInstruction(ShippingInstructionTO shippingInstructionTO) {

    try {
      shippingInstructionTO.pushCarrierBookingReferenceIntoCargoItemsIfNecessary();
    } catch (ConcreteRequestErrorMessageException e) {
      return Mono.error(e);
    }

    OffsetDateTime now = OffsetDateTime.now();
    ShippingInstruction shippingInstruction =
        shippingInstructionMapper.dtoToShippingInstruction(shippingInstructionTO);
    shippingInstruction.setDocumentStatus(ShipmentEventTypeCode.RECE);
    shippingInstruction.setShippingInstructionCreatedDateTime(now);
    shippingInstruction.setShippingInstructionUpdatedDateTime(now);

    return shippingInstructionRepository
        .save(shippingInstruction)
        .flatMap(
            si -> {
              if (shippingInstructionTO.getPlaceOfIssue() == null) return Mono.just(si);
              return locationService
                  .createLocationByTO(
                      shippingInstructionTO.getPlaceOfIssue(),
                      poi ->
                          shippingInstructionRepository.setPlaceOfIssueFor(
                              poi, si.getShippingInstructionID()))
                  .then(Mono.just(si));
            })
        .flatMap(
            si -> {
              if (shippingInstructionTO.getShipmentEquipments() == null) return Mono.just(si);
              String carrierBookingReference = getCarrierBookingReference(shippingInstructionTO);
              return shipmentRepository
                  .findByCarrierBookingReference(carrierBookingReference)
                  .flatMap(
                      x ->
                          shipmentEquipmentService
                              .createShipmentEquipment(
                                  x.getShipmentID(),
                                  si.getShippingInstructionID(),
                                  shippingInstructionTO.getShipmentEquipments())
                              .then(Mono.just(si)))
                  .thenReturn(si);
            })
        .flatMap(
            si -> {
              if (shippingInstructionTO.getDocumentParties() == null) return Mono.just(si);
              return documentPartyService
                  .createDocumentPartiesByShippingInstructionID(
                      si.getShippingInstructionID(), shippingInstructionTO.getDocumentParties())
                  .then(Mono.just(si));
            })
        .flatMap(
            si -> {
              if (shippingInstructionTO.getReferences() == null) return Mono.just(si);
              return referenceService
                  .createReferencesByShippingInstructionIDAndTOs(
                      si.getShippingInstructionID(), shippingInstructionTO.getReferences())
                  .then(Mono.just(si));
            })
        .flatMap(si -> createShipmentEvent(si).thenReturn(si))
        .map(shippingInstructionMapper::shippingInstructionToShippingInstructionResponseTO);
  }

  // TODO: fix once we know carrierBookingReference can be null (none on SI and no CargoItems)
  private String getCarrierBookingReference(ShippingInstructionTO shippingInstructionTO) {
    if (shippingInstructionTO.getCarrierBookingReference() == null) {
      List<CargoItemTO> cargoItems = new ArrayList<>();
      for (ShipmentEquipmentTO shipmentEquipmentTO :
          shippingInstructionTO.getShipmentEquipments()) {
        cargoItems.addAll(shipmentEquipmentTO.getCargoItems());
      }
      return cargoItems.get(0).getCarrierBookingReference();
    }
    return shippingInstructionTO.getCarrierBookingReference();
  }

  private Mono<ShipmentEvent> createShipmentEvent(ShippingInstruction shippingInstruction) {
    return shipmentEventFromShippingInstruction(shippingInstruction, null)
        .flatMap(shipmentEventService::create)
        .switchIfEmpty(
            Mono.error(
                ConcreteRequestErrorMessageException.invalidParameter(
                    "Failed to create shipment event for ShippingInstruction.")));
  }

  private Mono<ShipmentEvent> shipmentEventFromShippingInstruction(
      ShippingInstruction shippingInstruction, String reason) {
    ShipmentEvent shipmentEvent = new ShipmentEvent();
    shipmentEvent.setShipmentEventTypeCode(
        ShipmentEventTypeCode.valueOf(shippingInstruction.getDocumentStatus().name()));
    shipmentEvent.setDocumentTypeCode(DocumentTypeCode.SHI);
    shipmentEvent.setEventClassifierCode(EventClassifierCode.ACT);
    shipmentEvent.setEventType(null);
    shipmentEvent.setCarrierBookingReference(null);
    shipmentEvent.setDocumentID(shippingInstruction.getShippingInstructionID());
    shipmentEvent.setEventCreatedDateTime(OffsetDateTime.now());
    shipmentEvent.setEventDateTime(shippingInstruction.getShippingInstructionUpdatedDateTime());
    shipmentEvent.setReason(reason);
    return Mono.just(shipmentEvent);
  }

  @Override
  public Mono<ShippingInstructionTO> replaceOriginal(
      String shippingInstructionID, ShippingInstructionTO update) {
    return null;
  }
}
