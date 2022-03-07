package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.events.edocumentation.service.ShipmentService;
import org.dcsa.core.events.model.Booking;
import org.dcsa.core.events.model.ShipmentEvent;
import org.dcsa.core.events.model.ShippingInstruction;
import org.dcsa.core.events.model.enums.DocumentTypeCode;
import org.dcsa.core.events.model.enums.EventClassifierCode;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.core.events.model.transferobjects.*;
import org.dcsa.core.events.repository.BookingRepository;
import org.dcsa.core.events.service.*;
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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
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

  private final BookingRepository bookingRepository;

  // Mappers
  private final ShippingInstructionMapper shippingInstructionMapper;

  @Transactional(readOnly = true)
  @Override
  public Mono<ShippingInstructionTO> findById(String shippingInstructionReference) {
    return Mono.justOrEmpty(shippingInstructionReference)
        .flatMap(shippingInstructionRepository::findById)
        .switchIfEmpty(
            Mono.error(
                ConcreteRequestErrorMessageException.notFound(
                    "No Shipping Instruction found with ID: " + shippingInstructionReference)))
        .flatMap(
            si -> {
              ShippingInstructionTO siTO = shippingInstructionMapper.shippingInstructionToDTO(si);

              return Mono.when(
                      shippingInstructionRepository
                          .findCarrierBookingReferenceByShippingInstructionReference(
                              si.getShippingInstructionReference())
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
                          .findShipmentIDsByShippingInstructionReference(si.getShippingInstructionReference())
                          .flatMap(shipmentEquipmentService::findShipmentEquipmentByShipmentID)
                          .flatMap(Flux::fromIterable)
                          .collectList()
                          .doOnNext(siTO::setShipmentEquipments),
                      documentPartyService
                          .fetchDocumentPartiesByByShippingInstructionReference(
                              si.getShippingInstructionReference())
                          .doOnNext(siTO::setDocumentParties),
                      referenceService
                          .findByShippingInstructionReference(si.getShippingInstructionReference())
                          .doOnNext(siTO::setReferences),
                      shipmentService
                          .findByShippingInstructionReference(si.getShippingInstructionReference())
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

    return validateDocumentStatusOnBooking(shippingInstructionTO)
        .flatMap(ignored -> shippingInstructionRepository.save(shippingInstruction))
        .flatMap(si -> createShipmentEvent(si).thenReturn(si))
        .flatMap(
            si -> {
              shippingInstructionTO.setShippingInstructionReference(si.getShippingInstructionReference());
              shippingInstructionTO.setDocumentStatus(si.getDocumentStatus());
              shippingInstructionTO.setShippingInstructionCreatedDateTime(
                  si.getShippingInstructionCreatedDateTime());
              shippingInstructionTO.setShippingInstructionUpdatedDateTime(
                  si.getShippingInstructionUpdatedDateTime());
              return Mono.when(
                      insertLocationTO(
                              shippingInstructionTO.getPlaceOfIssue(),
                              shippingInstructionTO.getShippingInstructionReference())
                          .doOnNext(shippingInstructionTO::setPlaceOfIssue),
                      insertDocumentPartyTOs(
                              shippingInstructionTO.getDocumentParties(),
                              shippingInstructionTO.getShippingInstructionReference())
                          .doOnNext(shippingInstructionTO::setDocumentParties),
                      shipmentEquipmentService
                          .addShipmentEquipmentToShippingInstruction(
                              shippingInstructionTO.getShipmentEquipments(), shippingInstructionTO)
                          .doOnNext(shippingInstructionTO::setShipmentEquipments),
                      referenceService.createReferencesByShippingInstructionReferenceAndTOs(
                          shippingInstructionTO.getShippingInstructionReference(),
                          shippingInstructionTO.getReferences()))
                  .thenReturn(shippingInstructionTO);
            })
        .flatMap(createShipmentEventFromDocumentStatus)
        .map(shippingInstructionMapper::dtoToShippingInstructionResponseTO);
  }

  @Override
  public Mono<ShippingInstructionResponseTO> updateShippingInstructionByShippingInstructionReference(
      String shippingInstructionReference, ShippingInstructionTO shippingInstructionRequest) {

    try {
      shippingInstructionRequest.pushCarrierBookingReferenceIntoCargoItemsIfNecessary();
    } catch (IllegalStateException e) {
      return Mono.error(ConcreteRequestErrorMessageException.invalidParameter(e.getMessage()));
    }

    return validateDocumentStatusOnBooking(shippingInstructionRequest)
        .flatMap(ignored -> shippingInstructionRepository.findById(shippingInstructionReference))
        .switchIfEmpty(
            Mono.error(
                ConcreteRequestErrorMessageException.invalidParameter(
                    "No Shipping Instruction found with ID: " + shippingInstructionReference)))
        .flatMap(checkUpdateShippingInstructionStatus)
        .flatMap(si -> createShipmentEvent(si).thenReturn(si))
        .flatMap(
            si -> {
              shippingInstructionRequest.setShippingInstructionReference(si.getShippingInstructionReference());
              shippingInstructionRequest.setDocumentStatus(si.getDocumentStatus());
              shippingInstructionRequest.setShippingInstructionCreatedDateTime(
                  si.getShippingInstructionCreatedDateTime());
              shippingInstructionRequest.setShippingInstructionUpdatedDateTime(
                OffsetDateTime.now());
              return Mono.when(
                      locationService
                          .resolveLocationByTO(
                              si.getPlaceOfIssueID(),
                              shippingInstructionRequest.getPlaceOfIssue(),
                              placeOfIssue ->
                                  shippingInstructionRepository.setPlaceOfIssueFor(
                                      placeOfIssue, si.getShippingInstructionReference()))
                          .doOnNext(shippingInstructionRequest::setPlaceOfIssue),
                      shipmentEquipmentService
                          .resolveShipmentEquipmentsForShippingInstructionReference(
                              shippingInstructionRequest.getShipmentEquipments(),
                              shippingInstructionRequest)
                          .doOnNext(shippingInstructionRequest::setShipmentEquipments),
                      documentPartyService
                          .resolveDocumentPartiesForShippingInstructionReference(
                              si.getShippingInstructionReference(),
                              shippingInstructionRequest.getDocumentParties())
                          .doOnNext(shippingInstructionRequest::setDocumentParties),
                      referenceService
                          .resolveReferencesForShippingInstructionReference(
                              shippingInstructionRequest.getReferences(),
                              si.getShippingInstructionReference())
                          .doOnNext(shippingInstructionRequest::setReferences))
                  .thenReturn(shippingInstructionRequest);
            })
        .flatMap(createShipmentEventFromDocumentStatus)
      .flatMap(
        siTO -> {
          ShippingInstruction shippingInstruction =
            shippingInstructionMapper.dtoToShippingInstruction(shippingInstructionRequest);
          shippingInstruction.setShippingInstructionReference(siTO.getShippingInstructionReference());
          shippingInstruction.setDocumentStatus(siTO.getDocumentStatus());
          shippingInstruction.setShippingInstructionCreatedDateTime(
            siTO.getShippingInstructionCreatedDateTime());
          shippingInstruction.setShippingInstructionUpdatedDateTime(
            siTO.getShippingInstructionUpdatedDateTime());
          return shippingInstructionRepository.save(shippingInstruction).thenReturn(siTO);
        })
      .map(shippingInstructionMapper::dtoToShippingInstructionResponseTO);
  }

  Mono<List<Booking>> validateDocumentStatusOnBooking(ShippingInstructionTO shippingInstructionTO) {

    List<String> carrierBookingReferences;
    if (shippingInstructionTO.getCarrierBookingReference() != null) {
      carrierBookingReferences =
          Collections.singletonList(shippingInstructionTO.getCarrierBookingReference());
    } else {
      carrierBookingReferences =
          shippingInstructionTO.getShipmentEquipments().stream()
              .flatMap(x -> x.getCargoItems().stream().map(CargoItemTO::getCarrierBookingReference))
              .distinct()
              .collect(Collectors.toList());
    }

    return Flux.fromIterable(carrierBookingReferences)
        .flatMap(
            carrierBookingReference ->
                bookingRepository
                    .findAllByCarrierBookingReference(carrierBookingReference)
                    .flatMap(
                        booking -> {
                          if (!ShipmentEventTypeCode.CONF.equals(booking.getDocumentStatus())) {
                            return Mono.error(
                                ConcreteRequestErrorMessageException.invalidParameter(
                                    "DocumentStatus "
                                        + booking.getDocumentStatus()
                                        + " for booking "
                                        + booking.getCarrierBookingRequestReference()
                                        + " related to carrier booking reference "
                                        + carrierBookingReference
                                        + " is not in "
                                        + ShipmentEventTypeCode.CONF
                                        + " state!"));
                          }
                          return Mono.just(booking);
                        })
                    .switchIfEmpty(
                        Mono.error(
                            ConcreteRequestErrorMessageException.notFound(
                                "No bookings found for carrier booking reference: "
                                    + carrierBookingReference))))
        .collectList();
  }

  private Mono<LocationTO> insertLocationTO(LocationTO placeOfIssue, String shippingInstructionReference) {
    if (placeOfIssue == null) return Mono.empty();
    return locationService.createLocationByTO(
        placeOfIssue,
        poi -> shippingInstructionRepository.setPlaceOfIssueFor(poi, shippingInstructionReference));
  }

  private Mono<List<DocumentPartyTO>> insertDocumentPartyTOs(
      List<DocumentPartyTO> documentPartyTOs, String shippingInstructionReference) {
    if (documentPartyTOs == null) return Mono.empty();
    return documentPartyService.createDocumentPartiesByShippingInstructionReference(
        shippingInstructionReference, documentPartyTOs);
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
    return getShipmentEventFromShippingInstruction(reason, shippingInstruction.getDocumentStatus(), shippingInstruction.getShippingInstructionReference(), shippingInstruction.getShippingInstructionUpdatedDateTime());
  }

  static Mono<ShipmentEvent> getShipmentEventFromShippingInstruction(String reason, ShipmentEventTypeCode documentStatus, String shippingInstructionReference, OffsetDateTime shippingInstructionUpdatedDateTime) {
    ShipmentEvent shipmentEvent = new ShipmentEvent();
    shipmentEvent.setShipmentEventTypeCode(documentStatus);
    shipmentEvent.setDocumentTypeCode(DocumentTypeCode.SHI);
    shipmentEvent.setEventClassifierCode(EventClassifierCode.ACT);
    shipmentEvent.setEventType(null);
    shipmentEvent.setCarrierBookingReference(null);
    shipmentEvent.setDocumentID(shippingInstructionReference);
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
