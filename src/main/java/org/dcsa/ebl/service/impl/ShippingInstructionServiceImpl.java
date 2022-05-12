package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.events.edocumentation.model.transferobject.ConsignmentItemTO;
import org.dcsa.core.events.edocumentation.service.ConsignmentItemService;
import org.dcsa.core.events.model.Booking;
import org.dcsa.core.events.model.ShipmentEvent;
import org.dcsa.core.events.model.ShippingInstruction;
import org.dcsa.core.events.model.TransportDocument;
import org.dcsa.core.events.model.enums.DocumentTypeCode;
import org.dcsa.core.events.model.enums.EventClassifierCode;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.core.events.model.transferobjects.EquipmentTO;
import org.dcsa.core.events.model.transferobjects.ShippingInstructionTO;
import org.dcsa.core.events.model.transferobjects.UtilizedTransportEquipmentTO;
import org.dcsa.core.events.repository.BookingRepository;
import org.dcsa.core.events.repository.TransportDocumentRepository;
import org.dcsa.core.events.service.DocumentPartyService;
import org.dcsa.core.events.service.ReferenceService;
import org.dcsa.core.events.service.ShipmentEventService;
import org.dcsa.core.events.service.UtilizedTransportEquipmentService;
import org.dcsa.core.exception.ConcreteRequestErrorMessageException;
import org.dcsa.ebl.model.mappers.ShippingInstructionMapper;
import org.dcsa.ebl.model.transferobjects.ShippingInstructionResponseTO;
import org.dcsa.ebl.repository.ShippingInstructionRepository;
import org.dcsa.ebl.service.ShippingInstructionService;
import org.dcsa.skernel.model.enums.PartyFunction;
import org.dcsa.skernel.service.LocationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ShippingInstructionServiceImpl implements ShippingInstructionService {

  private final ShippingInstructionRepository shippingInstructionRepository;
  private final LocationService locationService;
  private final UtilizedTransportEquipmentService utilizedTransportEquipmentService;
  private final ShipmentEventService shipmentEventService;
  private final DocumentPartyService documentPartyService;
  private final ReferenceService referenceService;
  private final ConsignmentItemService consignmentItemService;

  private final BookingRepository bookingRepository;
  private final TransportDocumentRepository transportDocumentRepository;

  // Mappers
  private final ShippingInstructionMapper shippingInstructionMapper;

  @Transactional(readOnly = true)
  @Override
  public Mono<ShippingInstructionTO> findByReference(String shippingInstructionReference) {
    return findEditableShippingInstructionByShippingInstructionReference(
            shippingInstructionReference)
        .flatMap(this::getDeepObjectsForShippingInstruction);
  }

  private Mono<ShippingInstruction> findEditableShippingInstructionByShippingInstructionReference(
      String shippingInstructionReference) {
    return shippingInstructionRepository
        .findLatestShippingInstructionByShippingInstructionReference(shippingInstructionReference)
        .switchIfEmpty(
            Mono.error(
                ConcreteRequestErrorMessageException.notFound(
                    "No shipping instruction found with shipping instruction reference: "
                        + shippingInstructionReference)))
        .filter(td -> Objects.isNull(td.getValidUntil()))
        .switchIfEmpty(
            Mono.error(
                ConcreteRequestErrorMessageException.internalServerError(
                    "All shipping instructions are inactive, at least one active shipping instruction should be present.")));
  }

  @Transactional(readOnly = true)
  @Override
  public Mono<ShippingInstructionTO> findByID(UUID shippingInstructionID) {
    return Mono.justOrEmpty(shippingInstructionID)
        .flatMap(shippingInstructionRepository::findById)
        .switchIfEmpty(
            Mono.error(
                ConcreteRequestErrorMessageException.notFound(
                    "No Shipping Instruction found with ID: " + shippingInstructionID)))
        .flatMap(this::getDeepObjectsForShippingInstruction);
  }

  private Mono<ShippingInstructionTO> getDeepObjectsForShippingInstruction(
      ShippingInstruction shippingInstruction) {
    ShippingInstructionTO siTO =
        shippingInstructionMapper.shippingInstructionToDTO(shippingInstruction);

    return Mono.when(
            shippingInstructionRepository
                .findCarrierBookingReferenceByShippingInstructionID(shippingInstruction.getId())
                .collectList()
                .filter(strings -> strings.size() == 1)
                .doOnNext(cRefs -> siTO.setCarrierBookingReference(cRefs.get(0))),
            locationService
                .fetchLocationByID(shippingInstruction.getPlaceOfIssueID())
                .doOnNext(siTO::setPlaceOfIssue),
            shippingInstructionRepository
                .findShipmentIDsByShippingInstructionID(shippingInstruction.getId())
                .concatMap(
                    utilizedTransportEquipmentService::findUtilizedTransportEquipmentByShipmentID)
                .concatMap(Flux::fromIterable)
                .collectList()
                .doOnNext(siTO::setUtilizedTransportEquipments),
            documentPartyService
                .fetchDocumentPartiesByByShippingInstructionID(shippingInstruction.getId())
                .doOnNext(siTO::setDocumentParties),
            referenceService
                .findByShippingInstructionID(shippingInstruction.getId())
                .doOnNext(siTO::setReferences),
            consignmentItemService
                .fetchConsignmentItemsTOByShippingInstructionID(shippingInstruction.getId())
                .doOnNext(siTO::setConsignmentItems))
        .thenReturn(siTO);
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

    List<String> equipmentReferences =
        shippingInstructionTO.getUtilizedTransportEquipments().stream()
            .map(UtilizedTransportEquipmentTO::getEquipment)
            .map(EquipmentTO::getEquipmentReference)
            .toList();
    if (equipmentReferences.size() != equipmentReferences.stream().distinct().count()) {
      return Mono.error(
          ConcreteRequestErrorMessageException.invalidParameter(
              "Equipment references need to be unique!"));
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
                    .findByTransportDocumentID(tdReference)
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
        .flatMap(
            si ->
                shippingInstructionRepository.findById(
                    si.getId())) // need to fetch the created SI, so the reference is populated
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
              return createDeepObjectsForShippingInstruction(si, shippingInstructionTO);
            })
        .flatMap(
            siTO ->
                transitionDocumentStatusAndRaiseShipmentEvent(shippingInstruction.getId(), siTO))
        .flatMap(
            siTO -> {
              ShippingInstruction shippingInstruction2 =
                  shippingInstructionMapper.dtoToShippingInstruction(siTO);
              shippingInstruction2.setId(shippingInstruction.getId());
              shippingInstruction2.setShippingInstructionReference(
                  siTO.getShippingInstructionReference());
              shippingInstruction2.setDocumentStatus(siTO.getDocumentStatus());
              shippingInstruction2.setShippingInstructionCreatedDateTime(
                  siTO.getShippingInstructionCreatedDateTime());
              shippingInstruction2.setShippingInstructionUpdatedDateTime(
                  siTO.getShippingInstructionUpdatedDateTime());
              return shippingInstructionRepository
                  .save(shippingInstruction2)
                  .flatMap(si -> createTransportDocumentFromShippingInstructionTO(si.getId(), siTO))
                  .thenReturn(siTO);
            })
        .map(shippingInstructionMapper::dtoToShippingInstructionResponseTO);
  }

  @Override
  public Mono<ShippingInstructionResponseTO>
      updateShippingInstructionByShippingInstructionReference(
          String shippingInstructionReference, ShippingInstructionTO shippingInstructionRequest) {

    try {
      shippingInstructionRequest
          .pushCarrierBookingReferenceIntoUtilizedTransportEquipmentIfNecessary();
    } catch (IllegalStateException e) {
      return Mono.error(ConcreteRequestErrorMessageException.invalidParameter(e.getMessage()));
    }

    return validateDocumentStatusOnBooking(shippingInstructionRequest)
        .flatMap(
            ignored ->
                this.findEditableShippingInstructionByShippingInstructionReference(
                    shippingInstructionReference))
        .switchIfEmpty(
            Mono.error(
                ConcreteRequestErrorMessageException.invalidParameter(
                    "No Shipping Instruction found with reference: "
                        + shippingInstructionReference)))
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
        .flatMap(
            si -> {
              si.setValidUntil(OffsetDateTime.now());
              return shippingInstructionRepository.save(si);
            })
        .flatMap(
            si -> {
              si.setId(null);
              si.setValidUntil(null);
              return shippingInstructionRepository.save(si);
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
              return createDeepObjectsForShippingInstruction(si, shippingInstructionRequest)
                  .flatMap(siTO -> transitionDocumentStatusAndRaiseShipmentEvent(si.getId(), siTO))
                  .flatMap(
                      siTO -> {
                        ShippingInstruction shippingInstruction =
                            shippingInstructionMapper.dtoToShippingInstruction(
                                shippingInstructionRequest);
                        shippingInstruction.setId(si.getId());
                        shippingInstruction.setShippingInstructionReference(
                            siTO.getShippingInstructionReference());
                        shippingInstruction.setDocumentStatus(siTO.getDocumentStatus());
                        shippingInstruction.setShippingInstructionCreatedDateTime(
                            siTO.getShippingInstructionCreatedDateTime());
                        shippingInstruction.setShippingInstructionUpdatedDateTime(
                            siTO.getShippingInstructionUpdatedDateTime());
                        return shippingInstructionRepository
                            .save(shippingInstruction)
                            .flatMap(
                                savedSi ->
                                    createTransportDocumentFromShippingInstructionTO(
                                        savedSi.getId(), siTO))
                            .thenReturn(siTO);
                      });
            })
        .map(shippingInstructionMapper::dtoToShippingInstructionResponseTO);
  }

  private Mono<ShippingInstructionTO> createDeepObjectsForShippingInstruction(
      ShippingInstruction si, ShippingInstructionTO shippingInstructionTO) {
    return Mono.when(
            locationService
                .createLocationByTO(
                    shippingInstructionTO.getPlaceOfIssue(),
                    poi -> shippingInstructionRepository.setPlaceOfIssueFor(poi, si.getId()))
                .doOnNext(shippingInstructionTO::setPlaceOfIssue),
            documentPartyService
                .createDocumentPartiesByShippingInstructionID(
                    si.getId(), shippingInstructionTO.getDocumentParties())
                .doOnNext(shippingInstructionTO::setDocumentParties),
            utilizedTransportEquipmentService
                .addUtilizedTransportEquipmentToShippingInstruction(
                    shippingInstructionTO.getUtilizedTransportEquipments(), shippingInstructionTO)
                .doOnNext(shippingInstructionTO::setUtilizedTransportEquipments),
            referenceService.createReferencesByShippingInstructionIdAndTOs(
                si.getId(), shippingInstructionTO.getReferences())
            // Defer consignment items until utilizedTransportEquipments have been handled (we need
            // to ID from them
            // to process the cargo items which is done inside the consignment item service)
            )
        .then(
            Mono.defer(
                () ->
                    consignmentItemService.createConsignmentItemsByShippingInstructionIDAndTOs(
                        si.getId(),
                        shippingInstructionTO.getConsignmentItems(),
                        shippingInstructionTO.getUtilizedTransportEquipments())))
        .doOnNext(shippingInstructionTO::setConsignmentItems)
        .thenReturn(shippingInstructionTO);
  }

  Mono<List<Booking>> validateDocumentStatusOnBooking(ShippingInstructionTO shippingInstructionTO) {

    List<String> carrierBookingReferences;
    if (shippingInstructionTO.getCarrierBookingReference() != null) {
      carrierBookingReferences =
          Collections.singletonList(shippingInstructionTO.getCarrierBookingReference());
    } else {
      carrierBookingReferences =
          shippingInstructionTO.getConsignmentItems().stream()
              .map(ConsignmentItemTO::getCarrierBookingReference)
              .distinct()
              .collect(Collectors.toList());
    }

    return Flux.fromIterable(carrierBookingReferences)
        .flatMap(
            carrierBookingReference ->
                bookingRepository
                    .findCarrierBookingReferenceAndValidUntilIsNull(carrierBookingReference)
                    .switchIfEmpty(
                        Mono.error(
                            ConcreteRequestErrorMessageException.notFound(
                                "No booking found with carrier booking reference: "
                                    + carrierBookingReference)))
                    .filter(booking -> Objects.isNull(booking.getValidUntil()))
                    .switchIfEmpty(
                        Mono.error(
                            ConcreteRequestErrorMessageException.internalServerError(
                                "All bookings are inactive, at least one active booking should be present.")))
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

    List<UtilizedTransportEquipmentTO> utilizedTransportEquipmentTOS =
        Objects.requireNonNullElse(
            shippingInstructionTO.getUtilizedTransportEquipments(), Collections.emptyList());

    // Check if carrierBooking reference is only set on one place,
    // either shipping instruction or cargo item
    utilizedTransportEquipmentTOS.stream()
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
    utilizedTransportEquipmentTOS.stream()
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
    return shipmentEventFromShippingInstruction(shippingInstruction)
        .flatMap(shipmentEventService::create)
        .switchIfEmpty(
            Mono.error(
                ConcreteRequestErrorMessageException.invalidParameter(
                    "Failed to create shipment event for ShippingInstruction.")));
  }

  private Mono<ShippingInstructionTO> transitionDocumentStatusAndRaiseShipmentEvent(
      UUID shippingInstructionID, ShippingInstructionTO shippingInstructionTO) {
    List<String> validationResult = validateShippingInstruction(shippingInstructionTO);
    Mono<ShipmentEvent> shipmentEvent;
    if (!validationResult.isEmpty()) {
      if (shippingInstructionTO.getDocumentStatus() == ShipmentEventTypeCode.DRFT) {
        // UC5 / UC7 that was rejected goes back to DRFT.
        // TODO: We ought to rollback the TD at this point as well but that requires
        // versioning.
        shippingInstructionTO.setDocumentStatus(ShipmentEventTypeCode.DRFT);
      } else {
        shippingInstructionTO.setDocumentStatus(ShipmentEventTypeCode.PENU);
      }
    } else {
      if (shippingInstructionTO.getDocumentStatus() == ShipmentEventTypeCode.DRFT) {
        // UC5 / UC7 that was accepted goes directly to APPR.
        shippingInstructionTO.setDocumentStatus(ShipmentEventTypeCode.APPR);
      } else {
        shippingInstructionTO.setDocumentStatus(ShipmentEventTypeCode.DRFT);
      }
    }
    shipmentEvent =
        getShipmentEventFromShippingInstruction(
            String.join("\n", validationResult),
            shippingInstructionTO.getDocumentStatus(),
            shippingInstructionID,
            shippingInstructionTO.getShippingInstructionReference(),
            shippingInstructionTO.getShippingInstructionUpdatedDateTime());
    return shipmentEvent.map(shipmentEventService::create).thenReturn(shippingInstructionTO);
  }

  private Mono<ShipmentEvent> shipmentEventFromShippingInstruction(
      ShippingInstruction shippingInstruction) {
    return getShipmentEventFromShippingInstruction(
        null,
        shippingInstruction.getDocumentStatus(),
        shippingInstruction.getId(),
        shippingInstruction.getShippingInstructionReference(),
        shippingInstruction.getShippingInstructionUpdatedDateTime());
  }

  private Mono<ShipmentEvent> getShipmentEventFromShippingInstruction(
      String reason,
      ShipmentEventTypeCode documentStatus,
      UUID documentID,
      String documentReference,
      OffsetDateTime shippingInstructionUpdatedDateTime) {
    ShipmentEvent shipmentEvent = new ShipmentEvent();
    shipmentEvent.setShipmentEventTypeCode(documentStatus);
    shipmentEvent.setDocumentTypeCode(DocumentTypeCode.SHI);
    shipmentEvent.setEventClassifierCode(EventClassifierCode.ACT);
    shipmentEvent.setEventType(null);
    shipmentEvent.setDocumentID(documentID);
    shipmentEvent.setDocumentReference(documentReference);
    shipmentEvent.setEventCreatedDateTime(OffsetDateTime.now());
    shipmentEvent.setEventDateTime(shippingInstructionUpdatedDateTime);
    shipmentEvent.setReason(reason);
    return Mono.just(shipmentEvent);
  }

  private Mono<ShippingInstructionTO> createTransportDocumentFromShippingInstructionTO(
      UUID shippingInstructionID, ShippingInstructionTO shippingInstructionTO) {
    if (ShipmentEventTypeCode.DRFT.equals(shippingInstructionTO.getDocumentStatus())) {
      OffsetDateTime now = OffsetDateTime.now();
      TransportDocument transportDocument = new TransportDocument();
      transportDocument.setShippingInstructionID(shippingInstructionID);
      transportDocument.setTransportDocumentCreatedDateTime(now);
      transportDocument.setTransportDocumentUpdatedDateTime(now);
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
