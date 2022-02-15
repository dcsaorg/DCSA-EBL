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
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

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
  private final DisplayedAddressRepository displayedAddressRepository;
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
        .flatMap(si -> createShipmentEvent(si).thenReturn(si))
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
        .flatMap(createShipmentEventFromDocumentStatus)
        .map(shippingInstructionMapper::dtoToShippingInstructionResponseTO);
  }

  @Override
  public Mono<ShippingInstructionResponseTO> updateShippingInstructionByShippingInstructionID(
      String shippingInstructionID, ShippingInstructionTO shippingInstructionRequest) {

    return shippingInstructionRepository
        .findShippingInstructionByShippingInstructionID(shippingInstructionID)
        .switchIfEmpty(
            Mono.error(
                ConcreteRequestErrorMessageException.invalidParameter(
                    "No Shipping Instruction found with ID: " + shippingInstructionID)))
        .flatMap(checkUpdateShippingInstructionStatus)
        .flatMap(si -> createShipmentEvent(si).thenReturn(si))
        .flatMap(
            si -> {
              ShippingInstruction shippingInstruction =
                  shippingInstructionMapper.dtoToShippingInstruction(shippingInstructionRequest);
              shippingInstruction.setShippingInstructionID(si.getShippingInstructionID());
              shippingInstruction.setShippingInstructionCreatedDateTime(
                  si.getShippingInstructionCreatedDateTime());
              shippingInstruction.setShippingInstructionUpdatedDateTime(OffsetDateTime.now());
              shippingInstruction.setDocumentStatus(si.getDocumentStatus());
              return shippingInstructionRepository.save(shippingInstruction);
            })
        .flatMap(
            si -> {
              shippingInstructionRequest.setShippingInstructionID(si.getShippingInstructionID());
              shippingInstructionRequest.setDocumentStatus(si.getDocumentStatus());
              shippingInstructionRequest.setShippingInstructionCreatedDateTime(
                  si.getShippingInstructionCreatedDateTime());
              shippingInstructionRequest.setShippingInstructionUpdatedDateTime(
                  si.getShippingInstructionUpdatedDateTime());
              return Mono.when(
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
                  .thenReturn(shippingInstructionRequest);
            })
        .flatMap(createShipmentEventFromDocumentStatus)
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

  private Mono<List<ShipmentEquipmentTO>> resolveShipmentEquipmentsForShippingInstructionID(
      List<ShipmentEquipmentTO> shipmentEquipments, ShippingInstructionTO shippingInstructionTO) {
    return cargoItemRepository
        .findAllByShippingInstructionID(shippingInstructionTO.getShippingInstructionID())
        .flatMap(
            cargoItems ->
                Mono.when(
                        sealRepository.deleteAllByShipmentEquipmentID(
                            cargoItems.getShipmentEquipmentID()),
                        activeReeferSettingsRepository.deleteByShipmentEquipmentID(
                            cargoItems.getShipmentEquipmentID()))
                    .thenReturn(cargoItems))
        .flatMap(
            cargoItem ->
                shipmentEquipmentRepository.findShipmentEquipmentByShipmentID(
                    cargoItem.getShipmentEquipmentID()))
        .flatMap(
            cargoItem -> cargoItemRepository.deleteById(cargoItem.getId()).thenReturn(cargoItem))
        .flatMap(
            shipmentEquipment ->
                equipmentRepository
                    .deleteAllByEquipmentReference(shipmentEquipment.getEquipmentReference())
                    .thenReturn(shipmentEquipment))
        .flatMap(
            shipmentEquipment ->
                shipmentEquipmentRepository
                    .deleteShipmentEquipmentByShipmentID(shipmentEquipment.getShipmentID())
                    .thenReturn(new ShipmentEquipmentTO()))
        .collectList()
        .flatMap(x -> insertShipmentEquipmentTOs(shipmentEquipments, shippingInstructionTO));
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
        .findByShippingInstructionID(shippingInstructionID)
        .flatMap(
            documentParty ->
                displayedAddressRepository
                    .deleteAllByDocumentPartyID(documentParty.getId())
                    .thenReturn(documentParty))
        .flatMap(
            ignored -> documentPartyRepository.deleteByShippingInstructionID(shippingInstructionID))
        .then(insertDocumentPartyTOs(documentPartyTOs, shippingInstructionID));
  }

  private Mono<List<DocumentPartyTO>> insertDocumentPartyTOs(
      List<DocumentPartyTO> documentPartyTOs, String shippingInstructionID) {
    if (documentPartyTOs == null) return Mono.empty();
    return documentPartyService.createDocumentPartiesByShippingInstructionID(
        shippingInstructionID, documentPartyTOs);
  }

  List<String> validateShippingInstruction(ShippingInstructionTO shippingInstructionTO) {
    List<String> validationErrors = new ArrayList<>();

    // Check if number of copies and number of originals are set properly for a non-electronic
    // shipping instruction
    if (Objects.nonNull(shippingInstructionTO.getIsElectronic())
        && !shippingInstructionTO.getIsElectronic()) {
      if (Objects.isNull(shippingInstructionTO.getNumberOfCopies())) {
        validationErrors.add(
            "number of copies is required for non electronic shipping instructions.");
      }
      if (Objects.isNull(shippingInstructionTO.getNumberOfOriginals())) {
        validationErrors.add(
            "number of originals is required for non electronic shipping instructions.");
      }
    }

    Supplier<Stream<ShipmentEquipmentTO>> shipmentEquipmentTOStream =
        () ->
            Stream.ofNullable(shippingInstructionTO.getShipmentEquipments())
                .flatMap(shipmentEquipmentTOS -> Stream.ofNullable(shipmentEquipmentTOS.stream()))
                .flatMap(Function.identity());

    // Check if carrierBooking reference is only set on one place,
    // either shipping instruction or cargo item
    // ToDo needs refactoring in: https://dcsa.atlassian.net/browse/DDT-854
    shipmentEquipmentTOStream
        .get()
        .flatMap(shipmentEquipmentTO -> shipmentEquipmentTO.getCargoItems().stream())
        .map(CargoItemTO::getCarrierBookingReference)
        .forEach(
            carrierBookingReferenceOnCargoItem -> {
              if (Objects.nonNull(carrierBookingReferenceOnCargoItem)
                  && Objects.nonNull(shippingInstructionTO.getCarrierBookingReference())) {
                validationErrors.add(
                    "Carrier Booking Reference present in both shipping instruction as well as cargo items.");
              } else if (Objects.isNull(carrierBookingReferenceOnCargoItem)
                  && Objects.isNull(shippingInstructionTO.getCarrierBookingReference())) {
                validationErrors.add(
                    "Carrier Booking Reference not present on shipping instruction.");
              }
            });

    // Check if equipment tare weight is set on shipper owned equipment
    shipmentEquipmentTOStream
        .get()
        .forEach(
            shipmentEquipmentTO -> {
              if (shipmentEquipmentTO.getIsShipperOwned()
                  && Objects.isNull(shipmentEquipmentTO.getEquipment().getTareWeight())) {
                validationErrors.add(
                    "equipment tare weight is required for shipper owned equipment.");
              }
            });

    return validationErrors;
  }

  // TODO: fix once we know carrierBookingReference can be null (none on SI and no CargoItems)
  //  https://dcsa.atlassian.net/browse/DDT-854
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

  private Mono<ShipmentEvent> createShipmentEvent(ShippingInstruction shippingInstruction) {
    return createShipmentEvent(shippingInstruction, null);
  }

  private Mono<ShipmentEvent> createShipmentEvent(ShippingInstructionTO shippingInstructionTO) {
    return createShipmentEvent(
        shippingInstructionMapper.dtoToShippingInstruction(shippingInstructionTO), null);
  }

  private Mono<ShipmentEvent> createShipmentEvent(
      ShippingInstructionTO shippingInstructionTO, String reason) {
    return createShipmentEvent(
        shippingInstructionMapper.dtoToShippingInstruction(shippingInstructionTO), reason);
  }

  private Mono<ShipmentEvent> createShipmentEvent(
      ShippingInstruction shippingInstruction, String reason) {
    return shipmentEventFromShippingInstruction(shippingInstruction, reason)
        .flatMap(shipmentEventService::create)
        .switchIfEmpty(
            Mono.error(
                ConcreteRequestErrorMessageException.invalidParameter(
                    "Failed to create shipment event for ShippingInstruction.")));
  }

  private final Function<ShippingInstructionTO, Mono<ShippingInstructionTO>>
      createShipmentEventFromDocumentStatus =
          si -> {
            List<String> validationResult = validateShippingInstruction(si);
            Mono<ShipmentEvent> shipmentEvent;
            if (!validationResult.isEmpty()) {
              si.setDocumentStatus(ShipmentEventTypeCode.PENU);
              shipmentEvent = createShipmentEvent(si, String.join("\n", validationResult));
            } else {
              si.setDocumentStatus(ShipmentEventTypeCode.PENC);
              shipmentEvent = createShipmentEvent(si);
            }
            return shipmentEvent.thenReturn(si);
          };

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
