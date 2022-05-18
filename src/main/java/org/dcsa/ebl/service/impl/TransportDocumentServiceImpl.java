package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.events.edocumentation.model.transferobject.BookingTO;
import org.dcsa.core.events.edocumentation.service.*;
import org.dcsa.core.events.model.Booking;
import org.dcsa.core.events.model.ShipmentEvent;
import org.dcsa.core.events.model.TransportDocument;
import org.dcsa.core.events.model.enums.DocumentTypeCode;
import org.dcsa.core.events.model.enums.EventClassifierCode;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.core.events.model.transferobjects.ShippingInstructionTO;
import org.dcsa.core.events.repository.BookingRepository;
import org.dcsa.core.events.repository.TransportDocumentRepository;
import org.dcsa.core.events.service.ShipmentEventService;
import org.dcsa.core.exception.ConcreteRequestErrorMessageException;
import org.dcsa.core.service.impl.AsymmetricQueryServiceImpl;
import org.dcsa.ebl.model.TransportDocumentSummary;
import org.dcsa.ebl.model.mappers.TransportDocumentMapper;
import org.dcsa.ebl.model.transferobjects.TransportDocumentRefStatusTO;
import org.dcsa.ebl.model.transferobjects.TransportDocumentTO;
import org.dcsa.ebl.repository.ShippingInstructionRepository;
import org.dcsa.ebl.service.ShippingInstructionService;
import org.dcsa.ebl.service.TransportDocumentService;
import org.dcsa.skernel.model.Carrier;
import org.dcsa.skernel.model.enums.CarrierCodeListProvider;
import org.dcsa.skernel.repositority.CarrierRepository;
import org.dcsa.skernel.service.LocationService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class TransportDocumentServiceImpl
    extends AsymmetricQueryServiceImpl<
        TransportDocumentRepository, TransportDocument, TransportDocumentSummary, UUID>
    implements TransportDocumentService {

  private final TransportDocumentRepository transportDocumentRepository;
  private final CarrierRepository carrierRepository;
  private final BookingRepository bookingRepository;
  private final ShippingInstructionRepository shippingInstructionRepository;

  private final ShippingInstructionService shippingInstructionService;
  private final ChargeService chargeService;
  private final CarrierClauseService carrierClauseService;
  private final LocationService locationService;
  private final ShipmentService shipmentService;
  private final ShipmentEventService shipmentEventService;
  private final TransportService transportService;
  private final ShipmentLocationService shipmentLocationService;

  private final TransportDocumentMapper transportDocumentMapper;

  public TransportDocumentRepository getRepository() {
    return transportDocumentRepository;
  }

  @Override
  protected Mono<TransportDocumentSummary> mapDM2TO(TransportDocument transportDocument) {
    TransportDocumentSummary transportDocumentSummary =
        transportDocumentMapper.transportDocumentToTransportDocumentSummary(transportDocument);

    return shippingInstructionRepository
        .findById(transportDocument.getShippingInstructionID())
        .switchIfEmpty(
            Mono.error(
                ConcreteRequestErrorMessageException.internalServerError(
                    "No shipping instruction was found with reference: "
                        + transportDocument.getShippingInstructionID())))
        .flatMap(
            shippingInstruction -> {
              transportDocumentSummary.setDocumentStatus(shippingInstruction.getDocumentStatus());
              transportDocumentSummary.setShippingInstructionReference(
                  shippingInstruction.getShippingInstructionReference());
              return shippingInstructionRepository
                  .findCarrierBookingReferenceByShippingInstructionID(shippingInstruction.getId())
                  .collectList()
                  .doOnNext(transportDocumentSummary::setCarrierBookingReferences)
                  .thenReturn(transportDocumentSummary);
            })
        .flatMap(
            ignored -> {
              if (transportDocument.getCarrier() == null) return Mono.just(transportDocumentSummary);
              return carrierRepository
                  .findById(transportDocument.getCarrier())
                  .switchIfEmpty(
                      Mono.error(
                          ConcreteRequestErrorMessageException.internalServerError(
                              "No carrier found with issuer ID: " + transportDocument.getCarrier())))
                  .flatMap(
                      carrier -> {
                        if (carrier.getSmdgCode() != null) {
                          transportDocumentSummary.setIssuerCodeListProvider(
                              CarrierCodeListProvider.SMDG);
                          transportDocumentSummary.setIssuerCode(carrier.getSmdgCode());
                        } else if (carrier.getNmftaCode() != null) {
                          transportDocumentSummary.setIssuerCodeListProvider(
                              CarrierCodeListProvider.NMFTA);
                          transportDocumentSummary.setIssuerCode(carrier.getNmftaCode());
                        } else {
                          return Mono.error(
                              ConcreteRequestErrorMessageException.invalidParameter(
                                  "Unsupported carrier code list provider."));
                        }
                        return Mono.just(transportDocumentSummary);
                      });
            });
  }

  @Override
  public Mono<TransportDocumentTO> findByTransportDocumentReference(
      String transportDocumentReference) {

    return Mono.justOrEmpty(transportDocumentReference)
        .flatMap(this::findEditableTransportDocumentByTransportDocumentReference)
        .flatMap(
            transportDocument -> {
              TransportDocumentTO transportDocumentTO =
                  transportDocumentMapper.transportDocumentToDTO(transportDocument);
              return Mono.when(
                      Mono.justOrEmpty(transportDocument.getCarrier())
                          .flatMap(carrierRepository::findById)
                          .doOnNext(carrier -> setIssuerOnTransportDocument(transportDocumentTO, carrier)),
                      Mono.justOrEmpty(transportDocument.getPlaceOfIssue())
                          .flatMap(locationService::fetchLocationDeepObjByID)
                          .doOnNext(transportDocumentTO::setPlaceOfIssue),
                      shipmentLocationService
                          .fetchShipmentLocationByTransportDocumentID(transportDocument.getId())
                          .doOnNext(transportDocumentTO::setShipmentLocations),
                      shippingInstructionService
                          .findByID(transportDocument.getShippingInstructionID())
                          .switchIfEmpty(
                              Mono.error(
                                  ConcreteRequestErrorMessageException.notFound(
                                      "No shipping instruction found with shipping instruction reference: "
                                          + transportDocument.getShippingInstructionID())))
                          .doOnNext(transportDocumentTO::setShippingInstruction),
                      chargeService
                          .fetchChargesByTransportDocumentID(transportDocument.getId())
                          .collectList()
                          .doOnNext(transportDocumentTO::setCharges),
                      carrierClauseService
                          .fetchCarrierClausesByTransportDocumentID(transportDocument.getId())
                          .collectList()
                          .doOnNext(transportDocumentTO::setCarrierClauses))
                  .thenReturn(transportDocumentTO);
            })
        .flatMap(this::setTransportsOnTransportDocument);
  }

  private Mono<TransportDocumentTO> setTransportsOnTransportDocument(
      TransportDocumentTO transportDocumentTO) {
    String carrierBookingReference =
        getSingleCarrierBookingReferenceOnTransportDocument(transportDocumentTO);
    return transportService
        .findByCarrierBookingReference(carrierBookingReference)
        .collectList()
        .doOnNext(transportDocumentTO::setTransports)
        .thenReturn(transportDocumentTO);
  }

  // All consignmentItems inside a transportDocument share the same
  // transportplan. So we can take one CarrierBookingReference, either on the root of the SI or on
  // one of the consignmentItems
  private String getSingleCarrierBookingReferenceOnTransportDocument(
      TransportDocumentTO transportDocumentTO) {
    String carrierBookingReference =
        transportDocumentTO.getShippingInstruction().getCarrierBookingReference();
    if (carrierBookingReference == null) {
      carrierBookingReference =
          transportDocumentTO
              .getShippingInstruction()
              .getConsignmentItems()
              .get(0)
              .getCarrierBookingReference();
    }

    return carrierBookingReference;
  }

  void setIssuerOnTransportDocument(TransportDocumentTO transportDocumentTO, Carrier carrier) {
    if (Objects.nonNull(carrier.getSmdgCode())) {
      transportDocumentTO.setCarrierCode(carrier.getSmdgCode());
      transportDocumentTO.setCarrierCodeListProvider(CarrierCodeListProvider.SMDG);
    } else if (Objects.nonNull(carrier.getNmftaCode())) {
      transportDocumentTO.setCarrierCode(carrier.getNmftaCode());
      transportDocumentTO.setCarrierCodeListProvider(CarrierCodeListProvider.NMFTA);
    }
  }

  @Override
  public Mono<TransportDocumentRefStatusTO> approveTransportDocument(String transportDocumentReference) {

    OffsetDateTime now = OffsetDateTime.now();
    return findByTransportDocumentReference(transportDocumentReference)
        .switchIfEmpty(
            Mono.error(
                ConcreteRequestErrorMessageException.notFound(
                    "No Transport Document found with ID: " + transportDocumentReference)))
        .flatMap(tdTO -> validateDocumentStatusOnBooking(tdTO).thenReturn(tdTO))
        .flatMap(
            TdTO -> {
              if (TdTO.getShippingInstruction().getDocumentStatus() != ShipmentEventTypeCode.DRFT) {
                return Mono.error(
                    ConcreteRequestErrorMessageException.invalidParameter(
                        "Cannot Approve Transport Document with Shipping Instruction that is not in status DRFT"));
              }

              TdTO.setTransportDocumentUpdatedDateTime(now);
              ShippingInstructionTO shippingInstructionTO = TdTO.getShippingInstruction();
              shippingInstructionTO.setDocumentStatus(ShipmentEventTypeCode.APPR);
              shippingInstructionTO.setShippingInstructionUpdatedDateTime(now);

              return Mono.when(
                      shippingInstructionRepository.setDocumentStatusByReference(
                          shippingInstructionTO.getDocumentStatus(),
                          shippingInstructionTO.getShippingInstructionUpdatedDateTime(),
                          shippingInstructionTO.getShippingInstructionReference()),
                      shipmentService
                          .findByShippingInstructionReference(
                              TdTO.getShippingInstruction().getShippingInstructionReference())
                          .flatMap(
                              shipmentTOs -> {
                                // check if returned list is empty
                                // TODO: This check does not seem like it belongs here? (and if it
                                // does, it is not a 404 but a 500)
                                if (shipmentTOs.isEmpty()) {
                                  return Mono.error(
                                      ConcreteRequestErrorMessageException.notFound(
                                          "No shipments found for Shipping instruction of transport document reference: "
                                              + transportDocumentReference));
                                }
                                return Flux.fromIterable(shipmentTOs)
                                    .concatMap(
                                        shipmentTO ->
                                            getBooking(
                                                    shipmentTO.getBooking().getCarrierBookingRequestReference(),
                                                    TdTO.getShippingInstruction()
                                                        .getShippingInstructionReference()) //
                                                .flatMap(
                                                    booking -> {
                                                      BookingTO bookingTO = shipmentTO.getBooking();
                                                      bookingTO.setDocumentStatus(
                                                          ShipmentEventTypeCode.CMPL);
                                                      bookingTO.setBookingRequestUpdatedDateTime(
                                                          now);
                                                      String carrierBookingRequestReference =
                                                          bookingTO
                                                              .getCarrierBookingRequestReference();
                                                      return bookingRepository
                                                          .updateDocumentStatusAndUpdatedDateTimeForCarrierBookingRequestReference(
                                                              ShipmentEventTypeCode.CMPL,
                                                              carrierBookingRequestReference,
                                                              now)
                                                          .flatMap(
                                                              ignored ->
                                                                  createShipmentEventFromBookingTO(
                                                                      booking.getId(), bookingTO))
                                                          .thenReturn(bookingTO);
                                                    })
                                                .doOnNext(shipmentTO::setBooking))
                                    .then(Mono.just(shipmentTOs));
                              })
                          .thenReturn(shippingInstructionTO)
                          .doOnNext(TdTO::setShippingInstruction))
                  .thenReturn(TdTO);
            })
        .flatMap(tdTO -> createShipmentEventFromTransportDocumentTO(tdTO).thenReturn(tdTO))
        .map(transportDocumentMapper::dtoToTransportDocumentRefStatus);
  }

  private Mono<Booking> getBooking(
      String carrierBookingRequestReference, String shippingInstructionReference) {
    // Don't use ServiceClass - use Repository directly in order to throw internal error if
    // BookingReference does not exist.
    return bookingRepository
        .findByCarrierBookingRequestReferenceAndValidUntilIsNull(carrierBookingRequestReference)
        .switchIfEmpty(
            Mono.error(
                new IllegalStateException(
                    "The CarrierBookingRequestReference: "
                        + carrierBookingRequestReference
                        + " specified on ShippingInstruction:"
                        + shippingInstructionReference
                        + " does not exist!")));
  }

  private Mono<ShipmentEvent> shipmentEventFromBookingTO(
      UUID bookingID, BookingTO booking, String reason) {
    ShipmentEvent shipmentEvent = new ShipmentEvent();
    shipmentEvent.setShipmentEventTypeCode(
        ShipmentEventTypeCode.valueOf(booking.getDocumentStatus().name()));
    shipmentEvent.setDocumentTypeCode(DocumentTypeCode.CBR);
    shipmentEvent.setEventClassifierCode(EventClassifierCode.ACT);
    shipmentEvent.setEventType(null);
    shipmentEvent.setDocumentID(bookingID);
    shipmentEvent.setEventDateTime(booking.getBookingRequestUpdatedDateTime());
    shipmentEvent.setEventCreatedDateTime(OffsetDateTime.now());
    shipmentEvent.setDocumentReference(booking.getCarrierBookingRequestReference());
    shipmentEvent.setReason(reason);
    return Mono.just(shipmentEvent);
  }

  Mono<ShipmentEvent> createShipmentEventFromBookingTO(UUID bookingID, BookingTO bookingTO) {
    return shipmentEventFromBookingTO(bookingID, bookingTO, "Booking is approved")
        .flatMap(shipmentEventService::create);
  }

  private Mono<TransportDocument> findEditableTransportDocumentByTransportDocumentReference(
      String transportDocumentReference) {
    return transportDocumentRepository
        .findLatestTransportDocumentByTransportDocumentReference(transportDocumentReference)
        .switchIfEmpty(
            Mono.error(
                ConcreteRequestErrorMessageException.notFound(
                    "No transport document found with transport document reference: "
                        + transportDocumentReference)))
        .filter(td -> Objects.isNull(td.getValidUntil()))
        .switchIfEmpty(
            Mono.error(
                ConcreteRequestErrorMessageException.internalServerError(
                    "All transport documents are inactive, at least one active transport document should be present.")));
  }

  Mono<ShipmentEvent> createShipmentEventFromTransportDocumentTO(
      TransportDocumentTO transportDocumentTO) {
    return findEditableTransportDocumentByTransportDocumentReference(
            transportDocumentTO.getTransportDocumentReference())
        .flatMap(
            transportDocument ->
                shipmentEventFromTransportDocumentTO(
                    transportDocument.getId(),
                    transportDocumentTO,
                    "Transport document is approved"))
        .flatMap(shipmentEventService::create);
  }

  Mono<ShipmentEvent> shipmentEventFromTransportDocumentTO(
      UUID shippingInstructionID, TransportDocumentTO transportDocumentTO, String reason) {
    ShipmentEvent shipmentEvent = new ShipmentEvent();
    shipmentEvent.setShipmentEventTypeCode(
        ShipmentEventTypeCode.valueOf(
            transportDocumentTO.getShippingInstruction().getDocumentStatus().name()));
    shipmentEvent.setEventType(null);
    shipmentEvent.setEventClassifierCode(EventClassifierCode.ACT);
    shipmentEvent.setDocumentTypeCode(DocumentTypeCode.SHI);
    shipmentEvent.setDocumentID(shippingInstructionID);
    shipmentEvent.setEventDateTime(transportDocumentTO.getTransportDocumentUpdatedDateTime());
    shipmentEvent.setEventCreatedDateTime(OffsetDateTime.now());
    shipmentEvent.setDocumentReference(transportDocumentTO.getTransportDocumentReference());
    shipmentEvent.setReason(reason);
    return Mono.just(shipmentEvent);
  }

  Mono<List<Booking>> validateDocumentStatusOnBooking(TransportDocumentTO transportDocumentTO) {

    return bookingRepository
        .findAllByShippingInstructionReference(
            transportDocumentTO.getShippingInstruction().getShippingInstructionReference())
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
                            + transportDocumentTO.getTransportDocumentReference()
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
                        + transportDocumentTO.getTransportDocumentReference())))
        .collectList();
  }
}
