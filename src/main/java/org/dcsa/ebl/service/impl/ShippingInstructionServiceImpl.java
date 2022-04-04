package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.events.edocumentation.service.ConsignmentItemService;
import org.dcsa.core.events.edocumentation.service.ShipmentService;
import org.dcsa.core.events.model.Booking;
import org.dcsa.core.events.model.ShipmentEvent;
import org.dcsa.core.events.model.ShippingInstruction;
import org.dcsa.core.events.model.TransportDocument;
import org.dcsa.core.events.model.enums.DocumentTypeCode;
import org.dcsa.core.events.model.enums.EventClassifierCode;
import org.dcsa.core.events.model.enums.PartyFunction;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.core.events.model.transferobjects.DocumentPartyTO;
import org.dcsa.core.events.model.transferobjects.LocationTO;
import org.dcsa.core.events.model.transferobjects.ShippingInstructionTO;
import org.dcsa.core.events.model.transferobjects.UtilizedTransportEquipmentTO;
import org.dcsa.core.events.repository.BookingRepository;
import org.dcsa.core.events.repository.TransportDocumentRepository;
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
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
public class ShippingInstructionServiceImpl implements ShippingInstructionService {

  private final ShippingInstructionRepository shippingInstructionRepository;
  private final LocationService locationService;
  private final UtilizedTransportEquipmentService utilizedTransportEquipmentService;
  private final ShipmentEventService shipmentEventService;
  private final DocumentPartyService documentPartyService;
  private final ReferenceService referenceService;
  private final ShipmentService shipmentService;
  private final ConsignmentItemService consignmentItemService;

  private final BookingRepository bookingRepository;
  private final TransportDocumentRepository transportDocumentRepository;

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
                          .findShipmentIDsByShippingInstructionReference(
                              si.getShippingInstructionReference())
                          .flatMap(utilizedTransportEquipmentService::findUtilizedTransportEquipmentByShipmentID)
                          .flatMap(Flux::fromIterable)
                          .collectList()
                          .doOnNext(siTO::setUtilizedTransportEquipments),
                      documentPartyService
                          .fetchDocumentPartiesByByShippingInstructionReference(
                              si.getShippingInstructionReference())
                          .doOnNext(siTO::setDocumentParties),
                      referenceService
                          .findByShippingInstructionReference(si.getShippingInstructionReference())
                          .doOnNext(siTO::setReferences),
                      shipmentService
                          .findByShippingInstructionReference(si.getShippingInstructionReference())
                          .doOnNext(siTO::setShipments),
                      consignmentItemService
                          .fetchConsignmentItemsTOByShippingInstructionReference(
                              siTO.getShippingInstructionReference())
                          .doOnNext(siTO::setConsignmentItems))
                  .thenReturn(siTO);
            });
  }

  @Transactional
  @Override
  public Mono<ShippingInstructionResponseTO> createShippingInstruction(
      ShippingInstructionTO shippingInstructionTO) {

    try {
      shippingInstructionTO.pushCarrierBookingReferenceIntoUtilizedTransportEquipmentIfNecessary();
    } catch (IllegalStateException e) {
      return Mono.error(ConcreteRequestErrorMessageException.invalidParameter(e.getMessage()));
    }

    OffsetDateTime now = OffsetDateTime.now();
    ShippingInstruction shippingInstruction =
        shippingInstructionMapper.dtoToShippingInstruction(shippingInstructionTO);
    shippingInstruction.setDocumentStatus(ShipmentEventTypeCode.RECE);
    shippingInstruction.setShippingInstructionCreatedDateTime(now);
    shippingInstruction.setShippingInstructionUpdatedDateTime(now);

    return Mono.justOrEmpty(shippingInstructionTO.getAmendmentToTransportDocument())
        .flatMap(
            tdReference ->
                // If this is an amendment, verify that the TD exist and is in the correct status.
                shippingInstructionRepository
                    .findByTransportDocumentReference(tdReference)
                    .switchIfEmpty(
                        Mono.error(
                            ConcreteRequestErrorMessageException.invalidParameter(
                                "Shipping Instruction",
                                "amendmentToTransportDocument",
                                "Unknown Transport Document Reference: " + tdReference)))
                    .flatMap(
                        siForTD -> {
                          if (siForTD.getDocumentStatus() != ShipmentEventTypeCode.ISSU) {
                            return Mono.error(
                                ConcreteRequestErrorMessageException.invalidParameter(
                                    "Shipping Instruction",
                                    "amendmentToTransportDocument",
                                    "The referenced transport document ("
                                        + tdReference
                                        + ") must be in state ISSU, but had state: "
                                        + siForTD.getDocumentStatus()));
                          }
                          return Mono.empty();
                        }))
        .then(validateDocumentStatusOnBooking(shippingInstructionTO))
        .flatMap(ignored -> shippingInstructionRepository.save(shippingInstruction))
        .flatMap(si -> createShipmentEvent(si).thenReturn(si))
        .flatMap(
            si -> {
              shippingInstructionTO.setShippingInstructionReference(
                  si.getShippingInstructionReference());
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
                      utilizedTransportEquipmentService
                          .addUtilizedTransportEquipmentToShippingInstruction(
                              shippingInstructionTO.getUtilizedTransportEquipments(), shippingInstructionTO)
                          .doOnNext(shippingInstructionTO::setUtilizedTransportEquipments),
                      referenceService.createReferencesByShippingInstructionReferenceAndTOs(
                          shippingInstructionTO.getShippingInstructionReference(),
                          shippingInstructionTO.getReferences()))
                  .thenReturn(shippingInstructionTO);
            })
        .flatMap(createShipmentEventFromDocumentStatus)
        .flatMap(
            siTO -> {
              ShippingInstruction shippingInstruction2 =
                  shippingInstructionMapper.dtoToShippingInstruction(siTO);
              shippingInstruction2.setShippingInstructionReference(
                  siTO.getShippingInstructionReference());
              shippingInstruction2.setDocumentStatus(siTO.getDocumentStatus());
              shippingInstruction2.setShippingInstructionCreatedDateTime(
                  siTO.getShippingInstructionCreatedDateTime());
              shippingInstruction2.setShippingInstructionUpdatedDateTime(
                  siTO.getShippingInstructionUpdatedDateTime());
              return shippingInstructionRepository.save(shippingInstruction2).thenReturn(siTO);
            })
        .flatMap(this::createTransportDocumentFromShippingInstructionTO)
        .map(shippingInstructionMapper::dtoToShippingInstructionResponseTO);
  }

  @Override
  public Mono<ShippingInstructionResponseTO>
      updateShippingInstructionByShippingInstructionReference(
          String shippingInstructionReference, ShippingInstructionTO shippingInstructionRequest) {

    try {
      shippingInstructionRequest.pushCarrierBookingReferenceIntoUtilizedTransportEquipmentIfNecessary();
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
        .flatMap(
            si -> {
              // We do not allow the amendmentToTransportDocument to change in PUT. It is not
              // required and a lot of hassle.
              if (!Objects.equals(
                  si.getAmendmentToTransportDocument(),
                  shippingInstructionRequest.getAmendmentToTransportDocument())) {
                return Mono.error(
                    ConcreteRequestErrorMessageException.invalidParameter(
                        "Shipping Instruction",
                        "amendmentToTransportDocument",
                        "The update would change the value of amendmentToTransportDocument, which is not allowed."));
              }
              return Mono.just(si);
            })
        .flatMap(si -> createShipmentEvent(si).thenReturn(si))
        .flatMap(
            si -> {
              shippingInstructionRequest.setShippingInstructionReference(
                  si.getShippingInstructionReference());
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
                      utilizedTransportEquipmentService
                          .resolveUtilizedTransportEquipmentsForShippingInstructionReference(
                              shippingInstructionRequest.getUtilizedTransportEquipments(),
                              shippingInstructionRequest)
                          .doOnNext(shippingInstructionRequest::setUtilizedTransportEquipments),
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
              shippingInstruction.setShippingInstructionReference(
                  siTO.getShippingInstructionReference());
              shippingInstruction.setDocumentStatus(siTO.getDocumentStatus());
              shippingInstruction.setShippingInstructionCreatedDateTime(
                  siTO.getShippingInstructionCreatedDateTime());
              shippingInstruction.setShippingInstructionUpdatedDateTime(
                  siTO.getShippingInstructionUpdatedDateTime());
              return shippingInstructionRepository.save(shippingInstruction).thenReturn(siTO);
            })
        .flatMap(this::createTransportDocumentFromShippingInstructionTO)
        .map(shippingInstructionMapper::dtoToShippingInstructionResponseTO);
  }

  Mono<List<Booking>> validateDocumentStatusOnBooking(ShippingInstructionTO shippingInstructionTO) {

    List<String> carrierBookingReferences;
    if (shippingInstructionTO.getCarrierBookingReference() != null) {
      carrierBookingReferences =
          Collections.singletonList(shippingInstructionTO.getCarrierBookingReference());
    } else {
      carrierBookingReferences =
          shippingInstructionTO.getUtilizedTransportEquipments().stream()
              .map(UtilizedTransportEquipmentTO::getCarrierBookingReference)
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
                                "No booking found for carrier booking reference: "
                                    + carrierBookingReference))))
        .collectList();
  }

  private Mono<LocationTO> insertLocationTO(
      LocationTO placeOfIssue, String shippingInstructionReference) {
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

    // Check if documentParties is not empty and that partyFunction on DocumentParty set properly
    // for an electronic shipping instruction
    if (Objects.nonNull(shippingInstructionTO.getIsElectronic())
        && shippingInstructionTO.getIsElectronic()) {
      if (Objects.isNull(shippingInstructionTO.getDocumentParties())
          || shippingInstructionTO.getDocumentParties().isEmpty()) {
        validationErrors.add(
            "A documentParty with partyFunction=EBL is required for electronic shipping instructions.");
      }

      if (Objects.nonNull(shippingInstructionTO.getDocumentParties())) {
        long documentPartyCount =
            shippingInstructionTO.getDocumentParties().stream()
                .filter(x -> x.getPartyFunction().equals(PartyFunction.EBL))
                .count();
        if (documentPartyCount == 0) {
          validationErrors.add(
              "An EBL solution provider need to be specified in DocumentParties for electronic shipping instructions.");
        } else if (documentPartyCount > 1) {
          validationErrors.add("Only 1 EBL solution provider can be specified in DocumentParties.");
        }
      }
    }

    Supplier<Stream<UtilizedTransportEquipmentTO>> utilizedTransportEquipmentTOStream =
        () ->
            Stream.ofNullable(shippingInstructionTO.getUtilizedTransportEquipments())
                .flatMap(Collection::stream);

    // Check if carrierBooking reference is only set on one place,
    // either shipping instruction or cargo item
    utilizedTransportEquipmentTOStream
        .get()
        .map(UtilizedTransportEquipmentTO::getCarrierBookingReference)
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
    utilizedTransportEquipmentTOStream
        .get()
        .forEach(
            utilizedTransportEquipmentTO -> {
              if (utilizedTransportEquipmentTO.getIsShipperOwned()
                  && Objects.isNull(utilizedTransportEquipmentTO.getEquipment().getTareWeight())) {
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
              if (si.getDocumentStatus() == ShipmentEventTypeCode.DRFT) {
                // UC5 / UC7 that was rejected goes back to DRFT.
                // TODO: We ought to rollback the TD at this point as well but that requires versioning.
                si.setDocumentStatus(ShipmentEventTypeCode.DRFT);
              } else {
                si.setDocumentStatus(ShipmentEventTypeCode.PENU);
              }
              shipmentEvent = createShipmentEvent(si, String.join("\n", validationResult));
            } else {
              if (si.getDocumentStatus() == ShipmentEventTypeCode.DRFT) {
                // UC5 / UC7 that was accepted goes directly to APPR.
                si.setDocumentStatus(ShipmentEventTypeCode.APPR);
              } else {
                si.setDocumentStatus(ShipmentEventTypeCode.DRFT);
              }
              shipmentEvent = createShipmentEvent(si);
            }
            return shipmentEvent.thenReturn(si);
          };

  private Mono<ShipmentEvent> shipmentEventFromShippingInstruction(
      ShippingInstruction shippingInstruction, String reason) {
    return getShipmentEventFromShippingInstruction(
        reason,
        shippingInstruction.getDocumentStatus(),
        shippingInstruction.getShippingInstructionReference(),
        shippingInstruction.getShippingInstructionUpdatedDateTime());
  }

  static Mono<ShipmentEvent> getShipmentEventFromShippingInstruction(
      String reason,
      ShipmentEventTypeCode documentStatus,
      String shippingInstructionReference,
      OffsetDateTime shippingInstructionUpdatedDateTime) {
    ShipmentEvent shipmentEvent = new ShipmentEvent();
    shipmentEvent.setShipmentEventTypeCode(documentStatus);
    shipmentEvent.setDocumentTypeCode(DocumentTypeCode.SHI);
    shipmentEvent.setEventClassifierCode(EventClassifierCode.ACT);
    shipmentEvent.setEventType(null);
    shipmentEvent.setDocumentID(shippingInstructionReference);
    shipmentEvent.setEventCreatedDateTime(OffsetDateTime.now());
    shipmentEvent.setEventDateTime(shippingInstructionUpdatedDateTime);
    shipmentEvent.setReason(reason);
    return Mono.just(shipmentEvent);
  }

  private Mono<ShippingInstructionTO> createTransportDocumentFromShippingInstructionTO(
      ShippingInstructionTO shippingInstructionTO) {
    if (ShipmentEventTypeCode.DRFT.equals(shippingInstructionTO.getDocumentStatus())) {
      OffsetDateTime now = OffsetDateTime.now();
      TransportDocument transportDocument = new TransportDocument();
      transportDocument.setShippingInstructionReference(
          shippingInstructionTO.getShippingInstructionReference());
      transportDocument.setTransportDocumentRequestCreatedDateTime(now);
      transportDocument.setTransportDocumentRequestUpdatedDateTime(now);
      return transportDocumentRepository.save(transportDocument).thenReturn(shippingInstructionTO);
    } else {
      return Mono.just(shippingInstructionTO);
    }
  }

  private final Function<ShippingInstruction, Mono<ShippingInstruction>>
      checkUpdateShippingInstructionStatus =
          shippingInstruction -> {
            if (shippingInstruction.getDocumentStatus() == ShipmentEventTypeCode.PENU
              || shippingInstruction.getDocumentStatus() == ShipmentEventTypeCode.DRFT) {
              return Mono.just(shippingInstruction);
            }
            return Mono.error(
                ConcreteRequestErrorMessageException.invalidParameter(
                    "DocumentStatus needs to be set to "
                        + ShipmentEventTypeCode.PENU
                        + " or "
                        + ShipmentEventTypeCode.DRFT
                        + " when updating Shipping Instruction"));
          };
}
