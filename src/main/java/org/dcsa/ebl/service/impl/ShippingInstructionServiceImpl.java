package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.events.model.ShipmentEvent;
import org.dcsa.core.events.model.enums.DocumentTypeCode;
import org.dcsa.core.events.model.enums.EventClassifierCode;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.core.events.model.transferobjects.*;
import org.dcsa.core.events.repository.*;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

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
  private final DocumentPartyRepository documentPartyRepository;
  private final ReferenceRepository referenceRepository;
  private final CargoItemRepository cargoItemRepository;
  private final SealRepository sealRepository;
  private final EquipmentRepository equipmentRepository;
  private final ActiveReeferSettingsRepository activeReeferSettingsRepository;
  private final ShipmentEquipmentRepository shipmentEquipmentRepository;

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
              shippingInstructionTO.setShippingInstructionCreatedDateTime(
                  si.getShippingInstructionCreatedDateTime());
              shippingInstructionTO.setShippingInstructionUpdatedDateTime(
                  si.getShippingInstructionUpdatedDateTime());
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

  @Override
  public Mono<ShippingInstructionResponseTO> updateShippingInstructionByCarrierBookingReference(
      String shippingInstructionID, final ShippingInstructionTO shippingInstructionRequest) {

    shippingInstructionRequest.setShippingInstructionID(UUID.randomUUID().toString());

    return shippingInstructionRepository
        .findShippingInstructionByShippingInstructionID(shippingInstructionID)
        .switchIfEmpty(
            Mono.error(
                ConcreteRequestErrorMessageException.invalidParameter(
                    "No Shipping Instruction found with ID: " + shippingInstructionID)))
        .flatMap(checkUpdateShippingInstructionStatus)
        .flatMap(
            si -> {
              ShippingInstruction shippingInstruction =
                  shippingInstructionMapper.dtoToShippingInstruction(shippingInstructionRequest);
              shippingInstruction.setShippingInstructionUpdatedDateTime(OffsetDateTime.now());
              return shippingInstructionRepository.save(shippingInstruction).thenReturn(si);
            })
        .flatMap(
            si ->
                Mono.when(
                        locationService
                            .resolveLocationByTO(
                                si.getPlaceOfIssueID(),
                                shippingInstructionRequest.getPlaceOfIssue(),
                                placeOfIssue ->
                                    shippingInstructionRepository.setPlaceOfIssueFor(
                                        placeOfIssue, si.getShippingInstructionID()))
                            .doOnNext(shippingInstructionRequest::setPlaceOfIssue),
                        resolveShipmentEquipmentsForShippingInstructionID(
                                shippingInstructionRequest.getShipmentEquipments(),
                                shippingInstructionRequest)
                            .doOnNext(shippingInstructionRequest::setShipmentEquipments),
                        resolveDocumentPartiesForShippingInstructionID(
                                si.getShippingInstructionID(),
                                shippingInstructionRequest.getDocumentParties())
                            .doOnNext(shippingInstructionRequest::setDocumentParties),
                        resolveReferencesForShippingInstructionID(
                                si.getShippingInstructionID(),
                                shippingInstructionRequest.getReferences())
                            .doOnNext(shippingInstructionRequest::setReferences))
                    .thenReturn(shippingInstructionRequest))
        .flatMap(siTO -> shipmentEventFromShippingInstructionTO(siTO, null).thenReturn(siTO))
        .flatMap(
            siTO -> Mono.just(shippingInstructionMapper.dtoToShippingInstructionResponseTO(siTO)));
  }

  private Mono<List<ReferenceTO>> resolveReferencesForShippingInstructionID(
      String shippingInstructionID, List<ReferenceTO> referenceTOs) {
    if (referenceTOs == null) return Mono.empty();
    return referenceRepository
        .deleteByShippingInstructionID(shippingInstructionID)
        .then(insertReferenceTOs(referenceTOs, shippingInstructionID));
  }

  private Mono<List<ReferenceTO>> insertReferenceTOs(
      List<ReferenceTO> references, String shippingInstructionID) {
    if (references == null) return Mono.empty();
    return referenceService.createReferencesByShippingInstructionIDAndTOs(
        shippingInstructionID, references);
  }

  private Mono<List<ShipmentEquipmentTO>> resolveShipmentEquipmentsForShippingInstructionID(List<ShipmentEquipmentTO> shipmentEquipments, ShippingInstructionTO shippingInstructionTO) {
    return cargoItemRepository
        .findAllByShippingInstructionID(shippingInstructionTO.getShippingInstructionID())
        .flatMap(
            cargoItems ->
                Flux.fromIterable(cargoItems)
                    .flatMap(
                        cargoItem ->
                            Mono.when(
                                    sealRepository.deleteAllByShipmentEquipmentID(
                                        cargoItem.getShipmentEquipmentID()),
                                    activeReeferSettingsRepository.deleteByShipmentEquipmentID(
                                        cargoItem.getShipmentEquipmentID()))
                                .thenReturn(cargoItem))
                    .flatMap(
                        cargoItem ->
                            cargoItemRepository
                                .deleteById(cargoItem.getId())
                                .then(
                                    shipmentEquipmentRepository.findShipmentEquipmentByShipmentID(
                                        cargoItem.getShipmentEquipmentID())))
                    .flatMap(
                        shipmentEquipment ->
                            equipmentRepository
                                .deleteAllByEquipmentReference(
                                    shipmentEquipment.getEquipmentReference())
                                .thenReturn(shipmentEquipment))
                    .flatMap(
                        shipmentEquipment ->
                            shipmentEquipmentRepository
                                .deleteShipmentEquipmentByShipmentID(
                                    shipmentEquipment.getShipmentID())
                                .thenReturn(new ShipmentEquipmentTO()))
                    .collectList()).flatMap(x -> insertShipmentEquipmentTOs(shipmentEquipments, shippingInstructionTO));
  }

  private Mono<List<ShipmentEquipmentTO>> insertShipmentEquipmentTOs(
      List<ShipmentEquipmentTO> shipmentEquipments, ShippingInstructionTO shippingInstructionTO) {
    if (shipmentEquipments == null) return Mono.empty();
    String carrierBookingReference = getCarrierBookingReference(shippingInstructionTO);
    // TODO: we have a known bug here that needs to be addressed.
    //  carrierBookingReference can differ from each cargoItem.
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

  private Mono<List<DocumentPartyTO>> resolveDocumentPartiesForShippingInstructionID(
      String shippingInstructionID, List<DocumentPartyTO> documentPartyTOs) {

    // this will create orphan parties
    return documentPartyRepository
        .deleteByShippingInstructionID(shippingInstructionID)
        .then(insertDocumentPartyTOs(documentPartyTOs, shippingInstructionID));
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

  private final Function<ShippingInstruction, Mono<ShippingInstruction>>
      checkUpdateShippingInstructionStatus =
          shippingInstruction -> {
            if (shippingInstruction.getDocumentStatus() == ShipmentEventTypeCode.PENU) {
              return Mono.just(shippingInstruction);
            }
            return Mono.error(
                ConcreteRequestErrorMessageException.invalidParameter(
                    "DocumentStatus needs to be set to "
                        + ShipmentEventTypeCode.PENU
                        + " when updating Shipping Instruction"));
          };
}
