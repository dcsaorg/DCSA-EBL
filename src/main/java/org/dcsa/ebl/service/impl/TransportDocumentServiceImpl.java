package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.events.edocumentation.model.transferobject.BookingTO;
import org.dcsa.core.events.edocumentation.service.CarrierClauseService;
import org.dcsa.core.events.edocumentation.service.ChargeService;
import org.dcsa.core.events.edocumentation.service.ShipmentService;
import org.dcsa.core.events.model.Booking;
import org.dcsa.core.events.model.Carrier;
import org.dcsa.core.events.model.ShipmentEvent;
import org.dcsa.core.events.model.TransportDocument;
import org.dcsa.core.events.model.enums.CarrierCodeListProvider;
import org.dcsa.core.events.model.enums.DocumentTypeCode;
import org.dcsa.core.events.model.enums.EventClassifierCode;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.core.events.model.transferobjects.CargoItemTO;
import org.dcsa.core.events.model.transferobjects.ShippingInstructionTO;
import org.dcsa.core.events.repository.BookingRepository;
import org.dcsa.core.events.repository.CarrierRepository;
import org.dcsa.core.events.repository.TransportDocumentRepository;
import org.dcsa.core.events.service.LocationService;
import org.dcsa.core.events.service.ShipmentEventService;
import org.dcsa.core.exception.ConcreteRequestErrorMessageException;
import org.dcsa.core.service.impl.AsymmetricQueryServiceImpl;
import org.dcsa.ebl.model.TransportDocumentSummary;
import org.dcsa.ebl.model.mappers.TransportDocumentMapper;
import org.dcsa.ebl.model.transferobjects.TransportDocumentTO;
import org.dcsa.ebl.repository.ShippingInstructionRepository;
import org.dcsa.ebl.service.ShippingInstructionService;
import org.dcsa.ebl.service.TransportDocumentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.dcsa.ebl.service.impl.ShippingInstructionServiceImpl.getShipmentEventFromShippingInstruction;

@RequiredArgsConstructor
@Service
public class TransportDocumentServiceImpl
    extends AsymmetricQueryServiceImpl<
        TransportDocumentRepository, TransportDocument, TransportDocumentSummary, String>
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

  private final TransportDocumentMapper transportDocumentMapper;

  public TransportDocumentRepository getRepository() {
    return transportDocumentRepository;
  }

  @Transactional
  @Override
  public Mono<TransportDocumentTO> findById(String transportDocumentReference) {
    return Mono.empty();
  }

  @Override
  protected Mono<TransportDocumentSummary> mapDM2TO(TransportDocument transportDocument) {
    TransportDocumentSummary transportDocumentSummary =
        transportDocumentMapper.transportDocumentToTransportDocumentSummary(transportDocument);

    return shippingInstructionRepository
        .findById(transportDocumentSummary.getShippingInstructionReference())
        .switchIfEmpty(
            Mono.error(
                ConcreteRequestErrorMessageException.internalServerError(
                    "No shipping instruction was found with reference: "
                        + transportDocument.getShippingInstructionReference())))
        .flatMap(
            shippingInstruction -> {
              transportDocumentSummary.setDocumentStatus(shippingInstruction.getDocumentStatus());
              return shippingInstructionRepository
                  .findCarrierBookingReferenceByShippingInstructionReference(
                      shippingInstruction.getShippingInstructionReference())
                  .collectList()
                  .doOnNext(transportDocumentSummary::setCarrierBookingReferences)
                  .thenReturn(transportDocumentSummary);
            })
        .flatMap(
            ignored -> {
              if (transportDocument.getIssuer() == null) return Mono.just(transportDocumentSummary);
              return carrierRepository
                  .findById(transportDocument.getIssuer())
                  .switchIfEmpty(
                      Mono.error(
                          ConcreteRequestErrorMessageException.internalServerError(
                              "No carrier found with issuer ID: " + transportDocument.getIssuer())))
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
        .flatMap(transportDocumentRepository::findById)
        .flatMap(
            transportDocument -> {
              TransportDocumentTO transportDocumentTO =
                  transportDocumentMapper.transportDocumentToDTO(transportDocument);
              return Mono.when(
                      carrierRepository
                          .findById(transportDocument.getIssuer())
                          .doOnNext(
                              carrier -> {
                                setIssuerOnTransportDocument(transportDocumentTO, carrier);
                              }),
                      locationService
                          .fetchLocationDeepObjByID(transportDocument.getPlaceOfIssue())
                          .doOnNext(transportDocumentTO::setPlaceOfIssue),
                      shippingInstructionService
                          .findById(transportDocument.getShippingInstructionReference())
                          .switchIfEmpty(
                              Mono.error(
                                  ConcreteRequestErrorMessageException.notFound(
                                      "No shipping instruction found with shipping instruction reference: "
                                          + transportDocument.getShippingInstructionReference())))
                          .doOnNext(transportDocumentTO::setShippingInstruction),
                      chargeService
                          .fetchChargesByTransportDocumentReference(transportDocumentReference)
                          .collectList()
                          .doOnNext(transportDocumentTO::setCharges),
                      carrierClauseService
                          .fetchCarrierClausesByTransportDocumentReference(
                              transportDocumentReference)
                          .collectList()
                          .doOnNext(transportDocumentTO::setCarrierClauses))
                  .thenReturn(transportDocumentTO);
            });
  }

  void setIssuerOnTransportDocument(TransportDocumentTO transportDocumentTO, Carrier carrier) {
    if (Objects.nonNull(carrier.getSmdgCode())) {
      transportDocumentTO.setIssuerCode(carrier.getSmdgCode());
      transportDocumentTO.setIssuerCodeListProvider(CarrierCodeListProvider.SMDG);
    } else if (Objects.nonNull(carrier.getNmftaCode())) {
      transportDocumentTO.setIssuerCode(carrier.getNmftaCode());
      transportDocumentTO.setIssuerCodeListProvider(CarrierCodeListProvider.NMFTA);
    }
  }

  @Override
  public Mono<TransportDocumentTO> ApproveTransportDocument(String transportDocumentReference) {

    OffsetDateTime now = OffsetDateTime.now();
    return findByTransportDocumentReference(transportDocumentReference)
        .switchIfEmpty(
            Mono.error(
                ConcreteRequestErrorMessageException.notFound(
                    "No Transport Document found with ID: " + transportDocumentReference)))
        .flatMap(tdTO -> validateDocumentStatusOnBooking(tdTO.getShippingInstruction()).thenReturn(tdTO))
        .flatMap(
            TdTO -> {
              if (TdTO.getShippingInstruction().getDocumentStatus() != ShipmentEventTypeCode.PENA) {
                return Mono.error(
                    ConcreteRequestErrorMessageException.invalidParameter(
                        "Cannot Approve Transport Document with Shipping Instruction that is not in status PENA"));
              }

              TdTO.setTransportDocumentUpdatedDateTime(now);
              ShippingInstructionTO shippingInstructionTO = TdTO.getShippingInstruction();
              shippingInstructionTO.setDocumentStatus(ShipmentEventTypeCode.APPR);
              shippingInstructionTO.setShippingInstructionUpdatedDateTime(now);

              return Mono.when(
                      shippingInstructionRepository.setDocumentStatusByID(
                          shippingInstructionTO.getDocumentStatus(),
                          shippingInstructionTO.getShippingInstructionUpdatedDateTime(),
                          shippingInstructionTO.getShippingInstructionReference()),
                      shipmentService
                          .findByShippingInstructionReference(
                            TdTO.getShippingInstruction().getShippingInstructionReference())
                          .flatMap(
                              shipmentTOs -> {
                                // check if returned list is empty
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
                                                    shipmentTO.getCarrierBookingReference(),
                                                    TdTO.getShippingInstruction()
                                                        .getShippingInstructionReference()) //
                                                .flatMap(
                                                    ignored -> {
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
                                                          .thenReturn(bookingTO);
                                                    })
                                                .flatMap(
                                                    bookingTO ->
                                                        createShipmentEventFromBookingTO(bookingTO)
                                                            .thenReturn(bookingTO))
                                                .doOnNext(shipmentTO::setBooking))
                                    .then(Mono.just(shipmentTOs));
                              })
                          .flatMap(
                              shipmentTOs -> {
                                shippingInstructionTO.setShipments(shipmentTOs);
                                return createShipmentEventFromShippingInstruction(
                                        shippingInstructionTO)
                                    .thenReturn(shippingInstructionTO);
                              })
                          .doOnNext(TdTO::setShippingInstruction))
                  .thenReturn(TdTO);
            })
        .flatMap(TdTO -> createShipmentEventFromTransportDocumentTO(TdTO).thenReturn(TdTO));
  }

  private Mono<Booking> getBooking(
      String carrierBookingReference, String shippingInstructionReference) {
    // Don't use ServiceClass - use Repository directly in order to throw internal error if
    // BookingReference does not exist.
    return bookingRepository
        .findByCarrierBookingRequestReference(carrierBookingReference)
        .switchIfEmpty(
            Mono.error(
                new IllegalStateException(
                    "The CarrierBookingReference: "
                        + carrierBookingReference
                        + " specified on ShippingInstruction:"
                        + shippingInstructionReference
                        + " does not exist!")));
  }

  private Mono<ShipmentEvent> shipmentEventFromBookingTO(BookingTO booking, String reason) {
    ShipmentEvent shipmentEvent = new ShipmentEvent();
    shipmentEvent.setShipmentEventTypeCode(
        ShipmentEventTypeCode.valueOf(booking.getDocumentStatus().name()));
    shipmentEvent.setDocumentTypeCode(DocumentTypeCode.CBR);
    shipmentEvent.setEventClassifierCode(EventClassifierCode.ACT);
    shipmentEvent.setEventType(null);
    shipmentEvent.setCarrierBookingReference(null);
    shipmentEvent.setDocumentID(booking.getCarrierBookingRequestReference());
    shipmentEvent.setEventDateTime(booking.getBookingRequestUpdatedDateTime());
    shipmentEvent.setEventCreatedDateTime(OffsetDateTime.now());
    shipmentEvent.setReason(reason);
    return Mono.just(shipmentEvent);
  }

  Mono<ShipmentEvent> createShipmentEventFromBookingTO(BookingTO bookingTO) {

    return shipmentEventFromBookingTO(bookingTO, "Booking is approved")
        .flatMap(shipmentEventService::create)
        .switchIfEmpty(
            Mono.error(
                ConcreteRequestErrorMessageException.internalServerError(
                    "Failed to create shipment event for transport Document:"
                        + bookingTO.getCarrierBookingRequestReference())));
  }

  Mono<ShipmentEvent> createShipmentEventFromTransportDocumentTO(
      TransportDocumentTO transportDocumentTO) {

    return shipmentEventFromTransportDocumentTO(
            transportDocumentTO, "Transport document is approved")
        .flatMap(shipmentEventService::create)
        .switchIfEmpty(
            Mono.error(
                ConcreteRequestErrorMessageException.internalServerError(
                    "Failed to create shipment event for transport Document:"
                        + transportDocumentTO.getTransportDocumentReference())));
  }

  Mono<ShipmentEvent> shipmentEventFromTransportDocumentTO(
      TransportDocumentTO transportDocumentTO, String reason) {
    ShipmentEvent shipmentEvent = new ShipmentEvent();
    shipmentEvent.setShipmentEventTypeCode(
        ShipmentEventTypeCode.valueOf(
            transportDocumentTO.getShippingInstruction().getDocumentStatus().name()));
    shipmentEvent.setEventType(null);
    shipmentEvent.setEventClassifierCode(EventClassifierCode.ACT);
    shipmentEvent.setDocumentTypeCode(DocumentTypeCode.SHI);
    shipmentEvent.setCarrierBookingReference(transportDocumentTO.getTransportDocumentReference());
    shipmentEvent.setDocumentID(
        transportDocumentTO.getShippingInstruction().getShippingInstructionReference());
    shipmentEvent.setEventDateTime(transportDocumentTO.getTransportDocumentUpdatedDateTime());
    shipmentEvent.setEventCreatedDateTime(OffsetDateTime.now());
    return Mono.just(shipmentEvent);
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

  private Mono<ShipmentEvent> createShipmentEventFromShippingInstruction(
      ShippingInstructionTO shippingInstruction) {
    return shipmentEventFromShippingInstruction(
            shippingInstruction, "All bookings in shipping instruction are approved")
        .flatMap(shipmentEventService::create)
        .switchIfEmpty(
            Mono.error(
                ConcreteRequestErrorMessageException.invalidParameter(
                    "Failed to create shipment event for ShippingInstruction: "
                        + shippingInstruction.getShippingInstructionReference())));
  }

  private Mono<ShipmentEvent> shipmentEventFromShippingInstruction(
      ShippingInstructionTO shippingInstructionTO, String reason) {
    return getShipmentEventFromShippingInstruction(
        reason,
        shippingInstructionTO.getDocumentStatus(),
        shippingInstructionTO.getShippingInstructionReference(),
        shippingInstructionTO.getShippingInstructionUpdatedDateTime());
  }
}
