package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.events.edocumentation.service.ShipmentService;
import org.dcsa.core.events.model.ShipmentEvent;
import org.dcsa.core.events.model.ShippingInstruction;
import org.dcsa.core.events.model.enums.DocumentTypeCode;
import org.dcsa.core.events.model.enums.EventClassifierCode;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.core.events.model.transferobjects.CargoItemTO;
import org.dcsa.core.events.model.transferobjects.DocumentPartyTO;
import org.dcsa.core.events.model.transferobjects.LocationTO;
import org.dcsa.core.events.model.transferobjects.ShipmentEquipmentTO;
import org.dcsa.core.events.model.transferobjects.ShippingInstructionTO;
import org.dcsa.core.events.service.DocumentPartyService;
import org.dcsa.core.events.service.LocationService;
import org.dcsa.core.events.service.ReferenceService;
import org.dcsa.core.events.service.ShipmentEquipmentService;
import org.dcsa.core.events.service.ShipmentEventService;
import org.dcsa.core.exception.ConcreteRequestErrorMessageException;
import org.dcsa.ebl.model.mappers.ShippingInstructionMapper;
import org.dcsa.ebl.model.transferobjects.ShippingInstructionResponseTO;
import org.dcsa.ebl.repository.ShippingInstructionRepository;
import org.dcsa.ebl.service.ShippingInstructionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
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
  private final ShipmentService shipmentService;

  // Mappers
  private final ShippingInstructionMapper shippingInstructionMapper;

  @Transactional(readOnly = true)
  @Override
  public Mono<ShippingInstructionTO> findById(String shippingInstructionID) {
    return Mono.justOrEmpty(shippingInstructionID)
        .flatMap(shippingInstructionRepository::findById)
        .switchIfEmpty(
            Mono.error(
                ConcreteRequestErrorMessageException.notFound(
                    "No Shipping Instruction found with ID: " + shippingInstructionID)))
        .flatMap(
            si -> {
              ShippingInstructionTO siTO = shippingInstructionMapper.shippingInstructionToDTO(si);

              return Mono.when(
                      shippingInstructionRepository
                          .findCarrierBookingReferenceByShippingInstructionID(
                              si.getShippingInstructionID())
                          .collectList()
                          .doOnNext(
                              cRefs -> {
                                // we should only set carrier booking reference on root SI TO if
                                // there is only one distinct value
                                // This is as per requirement
                                if (cRefs.size() == 1) {
                                  siTO.setCarrierBookingReference(cRefs.get(0));
                                }
                              }),
                      locationService
                          .fetchLocationByID(si.getPlaceOfIssueID())
                          .doOnNext(siTO::setPlaceOfIssue),
                      shippingInstructionRepository
                          .findShipmentIDsByShippingInstructionID(si.getShippingInstructionID())
                          .flatMap(shipmentEquipmentService::findShipmentEquipmentByShipmentID)
                          .flatMap(Flux::fromIterable)
                          .collectList()
                          .doOnNext(siTO::setShipmentEquipments),
                      documentPartyService
                          .fetchDocumentPartiesByByShippingInstructionID(
                              si.getShippingInstructionID())
                          .doOnNext(siTO::setDocumentParties),
                      referenceService
                          .findByShippingInstructionID(si.getShippingInstructionID())
                          .doOnNext(siTO::setReferences),
                      shipmentService
                          .findByShippingInstructionID(si.getShippingInstructionID())
                          .doOnNext(siTO::setShipments))
                  .thenReturn(siTO);
            });
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
                      shipmentEquipmentService
                          .addShipmentEquipmentToShippingInstruction(
                              shippingInstructionTO.getShipmentEquipments(), shippingInstructionTO)
                          .doOnNext(shippingInstructionTO::setShipmentEquipments),
                      referenceService.createReferencesByShippingInstructionIDAndTOs(
                          shippingInstructionTO.getShippingInstructionID(),
                          shippingInstructionTO.getReferences()))
                  .thenReturn(shippingInstructionTO);
            })
        .flatMap(createShipmentEventFromDocumentStatus)
        .map(shippingInstructionMapper::dtoToShippingInstructionResponseTO);
  }

  @Override
  public Mono<ShippingInstructionResponseTO> updateShippingInstructionByShippingInstructionID(
      String shippingInstructionID, ShippingInstructionTO shippingInstructionRequest) {

    try {
      shippingInstructionRequest.pushCarrierBookingReferenceIntoCargoItemsIfNecessary();
    } catch (IllegalStateException e) {
      return Mono.error(ConcreteRequestErrorMessageException.invalidParameter(e.getMessage()));
    }

    return shippingInstructionRepository
        .findById(shippingInstructionID)
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
                      shipmentEquipmentService
                          .resolveShipmentEquipmentsForShippingInstructionID(
                              shippingInstructionRequest.getShipmentEquipments(),
                              shippingInstructionRequest)
                          .doOnNext(shippingInstructionRequest::setShipmentEquipments),
                      documentPartyService
                          .resolveDocumentPartiesForShippingInstructionID(
                              si.getShippingInstructionID(),
                              shippingInstructionRequest.getDocumentParties())
                          .doOnNext(shippingInstructionRequest::setDocumentParties),
                      referenceService
                          .resolveReferencesForShippingInstructionID(
                              shippingInstructionRequest.getReferences(),
                              si.getShippingInstructionID())
                          .doOnNext(shippingInstructionRequest::setReferences))
                  .thenReturn(shippingInstructionRequest);
            })
        .flatMap(createShipmentEventFromDocumentStatus)
        .flatMap(
            siTO -> Mono.just(shippingInstructionMapper.dtoToShippingInstructionResponseTO(siTO)));
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
    return getShipmentEventFromShippingInstruction(reason, shippingInstruction.getDocumentStatus(), shippingInstruction.getShippingInstructionID(), shippingInstruction.getShippingInstructionUpdatedDateTime());
  }

  static Mono<ShipmentEvent> getShipmentEventFromShippingInstruction(String reason, ShipmentEventTypeCode documentStatus, String shippingInstructionID, OffsetDateTime shippingInstructionUpdatedDateTime) {
    ShipmentEvent shipmentEvent = new ShipmentEvent();
    shipmentEvent.setShipmentEventTypeCode(
        ShipmentEventTypeCode.valueOf(documentStatus.name()));
    shipmentEvent.setDocumentTypeCode(DocumentTypeCode.SHI);
    shipmentEvent.setEventClassifierCode(EventClassifierCode.ACT);
    shipmentEvent.setEventType(null);
    shipmentEvent.setCarrierBookingReference(null);
    shipmentEvent.setDocumentID(shippingInstructionID);
    shipmentEvent.setEventCreatedDateTime(OffsetDateTime.now());
    shipmentEvent.setEventDateTime(shippingInstructionUpdatedDateTime);
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
