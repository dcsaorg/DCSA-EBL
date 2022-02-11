package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.events.model.ShipmentEvent;
import org.dcsa.core.events.model.enums.DocumentTypeCode;
import org.dcsa.core.events.model.enums.EventClassifierCode;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.core.events.model.transferobjects.*;
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
  public Mono<ShippingInstructionResponseTO> createShippingInstruction(
      ShippingInstructionTO shippingInstructionTO) {

    try {
      shippingInstructionTO.pushCarrierBookingReferenceIntoCargoItemsIfNecessary();
    } catch (IllegalStateException e) {
      return Mono.error(ConcreteRequestErrorMessageException.invalidParameter(e.getMessage()));
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
              shippingInstructionTO.setShippingInstructionID(si.getShippingInstructionID());
              shippingInstructionTO.setDocumentStatus(si.getDocumentStatus());
              shippingInstructionTO.setShippingInstructionCreatedDateTime(si.getShippingInstructionCreatedDateTime());
              shippingInstructionTO.setShippingInstructionUpdatedDateTime(si.getShippingInstructionUpdatedDateTime());
              return Mono.when(
                      insertLocationTO(
                              shippingInstructionTO.getPlaceOfIssue(),
                              shippingInstructionTO.getShippingInstructionID())
                          .doOnNext(shippingInstructionTO::setPlaceOfIssue),
                      insertDocumentPartyTOs(
                              shippingInstructionTO.getDocumentParties(),
                              shippingInstructionTO.getShippingInstructionID())
                          .doOnNext(shippingInstructionTO::setDocumentParties),
                      insertShipmentEquipmentTOs(
                              shippingInstructionTO.getShipmentEquipments(), shippingInstructionTO)
                          .doOnNext(shippingInstructionTO::setShipmentEquipments),
                      insertReferenceTOs(
                          shippingInstructionTO.getReferences(),
                          shippingInstructionTO.getShippingInstructionID()))
                  .thenReturn(shippingInstructionTO);
            })
        .flatMap(si -> createShipmentEvent(si).thenReturn(si))
        .map(shippingInstructionMapper::dtoToShippingInstructionResponseTO);
  }

  private Mono<List<ReferenceTO>> insertReferenceTOs(
      List<ReferenceTO> references, String shippingInstructionID) {
    if (references == null) return Mono.empty();
    return referenceService.createReferencesByShippingInstructionIDAndTOs(
        shippingInstructionID, references);
  }

  private Mono<List<ShipmentEquipmentTO>> insertShipmentEquipmentTOs(
      List<ShipmentEquipmentTO> shipmentEquipments, ShippingInstructionTO shippingInstructionTO) {
    if (shipmentEquipments == null) return Mono.empty();
    String carrierBookingReference = getCarrierBookingReference(shippingInstructionTO);
    // TODO: we have a known bug here that needs to be addressed (if
    // carrierBookingReference differs from each cargoItem)
    return shipmentRepository
        .findByCarrierBookingReference(carrierBookingReference)
        .switchIfEmpty(
            Mono.error(
                ConcreteRequestErrorMessageException.invalidParameter(
                    "No shipment found with carrierBookingReference: " + carrierBookingReference)))
        .flatMap(
            x ->
                shipmentEquipmentService.createShipmentEquipment(
                    x.getShipmentID(),
                    shippingInstructionTO.getShippingInstructionID(),
                    shipmentEquipments));
  }

  private Mono<LocationTO> insertLocationTO(LocationTO placeOfIssue, String shippingInstructionID) {
    if (placeOfIssue == null) return Mono.empty();
    return locationService.createLocationByTO(
        placeOfIssue,
        poi -> shippingInstructionRepository.setPlaceOfIssueFor(poi, shippingInstructionID));
  }

  private Mono<List<DocumentPartyTO>> insertDocumentPartyTOs(
      List<DocumentPartyTO> documentPartyTOs, String shippingInstructionID) {
    if (documentPartyTOs == null) return Mono.empty();
    return documentPartyService.createDocumentPartiesByShippingInstructionID(
        shippingInstructionID, documentPartyTOs);
  }

  // TODO: fix once we know carrierBookingReference can be null (none on SI and no CargoItems)
  String getCarrierBookingReference(ShippingInstructionTO shippingInstructionTO) {
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

  private Mono<ShipmentEvent> createShipmentEvent(ShippingInstructionTO shippingInstructionTO) {
    return shipmentEventFromShippingInstructionTO(shippingInstructionTO, null)
        .flatMap(shipmentEventService::create)
        .switchIfEmpty(
            Mono.error(
                ConcreteRequestErrorMessageException.invalidParameter(
                    "Failed to create shipment event for ShippingInstruction.")));
  }

  private Mono<ShipmentEvent> shipmentEventFromShippingInstructionTO(
      ShippingInstructionTO shippingInstructionTO, String reason) {
    ShipmentEvent shipmentEvent = new ShipmentEvent();
    shipmentEvent.setShipmentEventTypeCode(
        ShipmentEventTypeCode.valueOf(shippingInstructionTO.getDocumentStatus().name()));
    shipmentEvent.setDocumentTypeCode(DocumentTypeCode.SHI);
    shipmentEvent.setEventClassifierCode(EventClassifierCode.ACT);
    shipmentEvent.setEventType(null);
    shipmentEvent.setCarrierBookingReference(null);
    shipmentEvent.setDocumentID(shippingInstructionTO.getShippingInstructionID());
    shipmentEvent.setEventCreatedDateTime(OffsetDateTime.now());
    shipmentEvent.setEventDateTime(shippingInstructionTO.getShippingInstructionUpdatedDateTime());
    shipmentEvent.setReason(reason);
    return Mono.just(shipmentEvent);
  }

  @Override
  public Mono<ShippingInstructionTO> replaceOriginal(
      String shippingInstructionID, ShippingInstructionTO update) {
    return null;
  }
}
