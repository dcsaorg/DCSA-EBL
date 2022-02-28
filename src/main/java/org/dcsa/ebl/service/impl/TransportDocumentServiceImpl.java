package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.dcsa.core.events.model.transferobjects.ShippingInstructionTO;
import org.dcsa.core.events.repository.BookingRepository;
import org.dcsa.core.events.repository.CarrierRepository;
import org.dcsa.core.events.repository.TransportDocumentRepository;
import org.dcsa.core.events.service.LocationService;
import org.dcsa.core.events.service.ShipmentEventService;
import org.dcsa.core.exception.ConcreteRequestErrorMessageException;
import org.dcsa.core.service.impl.QueryServiceImpl;
import org.dcsa.ebl.model.mappers.TransportDocumentMapper;
import org.dcsa.ebl.model.transferobjects.TransportDocumentTO;
import org.dcsa.ebl.repository.ShippingInstructionRepository;
import org.dcsa.ebl.service.ShippingInstructionService;
import org.dcsa.ebl.service.TransportDocumentService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class TransportDocumentServiceImpl
    extends QueryServiceImpl<TransportDocumentRepository, TransportDocument, String>
    implements TransportDocumentService {

  private final TransportDocumentRepository transportDocumentRepository;
  private final CarrierRepository carrierRepository;
  private final ShippingInstructionService shippingInstructionService;
  private final ChargeService chargeService;
  private final CarrierClauseService carrierClauseService;
  private final LocationService locationService;
  private Log log = LogFactory.getLog(TransportDocumentServiceImpl.class);

  private final TransportDocumentMapper transportDocumentMapper;
  private final BookingRepository bookingRepository;
  private final ShippingInstructionRepository shippingInstructionRepository;
  private final ShipmentService shipmentService;
  private final ShipmentEventService shipmentEventService;

  public TransportDocumentRepository getRepository() {
    return transportDocumentRepository;
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
                          .findById(transportDocument.getShippingInstructionID())
                          .switchIfEmpty(
                              Mono.error(
                                  ConcreteRequestErrorMessageException.notFound(
                                      "No shipping instruction found with shipping instruction id: "
                                          + transportDocument.getShippingInstructionID())))
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

  public Mono<TransportDocumentTO> ApproveTransportDocument(String transportDocumentReference) {

    OffsetDateTime now = OffsetDateTime.now();
    return findByTransportDocumentReference(transportDocumentReference)
        .switchIfEmpty(
            Mono.error(
                ConcreteRequestErrorMessageException.notFound(
                    "No Transport Document found with ID: " + transportDocumentReference)))
        .flatMap(
            TdTO -> {
              if (TdTO.getShippingInstruction().getDocumentStatus() != ShipmentEventTypeCode.PENA) {
                return Mono.error(
                    ConcreteRequestErrorMessageException.invalidParameter(
                        "Cannot Approve Transport Document with Shipping Instruction that is not in status PENA"));
              }

              ShippingInstructionTO shippingInstructionTO = TdTO.getShippingInstruction();
              shippingInstructionTO.setDocumentStatus(ShipmentEventTypeCode.APPR);
              shippingInstructionTO.setShippingInstructionUpdatedDateTime(now);

              return Mono.when(
                      shippingInstructionRepository.setDocumentStatusByID(
                          shippingInstructionTO.getDocumentStatus(),
                          shippingInstructionTO.getShippingInstructionUpdatedDateTime(),
                          shippingInstructionTO.getShippingInstructionID()),
                      shipmentService
                          .findByShippingInstructionID(
                              TdTO.getShippingInstruction().getShippingInstructionID())
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
                                                        .getShippingInstructionID()) //
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
                                                .doOnNext(shipmentTO::setBooking))
                                    .then(Mono.just(shipmentTOs));
                              })
                          .flatMap(
                              shippings -> {
                                shippingInstructionTO.setShipments(shippings);
                                return Mono.just(shippingInstructionTO);
                              })
                          .doOnNext(TdTO::setShippingInstruction))
                  .thenReturn(TdTO);
            })
        .flatMap(TdTO -> createShipmentEventFromTransportDocumentTO(TdTO).thenReturn(TdTO));
  }

  private Mono<Booking> getBooking(String carrierBookingReference, String shippingInstructionID) {
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
                        + shippingInstructionID
                        + " does not exist!")));
  }

  Mono<ShipmentEvent> createShipmentEventFromTransportDocumentTO(
      TransportDocumentTO transportDocumentTO) {

    return shipmentEventFromTransportDocumentTO(transportDocumentTO)
        .flatMap(shipmentEventService::create)
        .switchIfEmpty(
            Mono.error(
                ConcreteRequestErrorMessageException.internalServerError(
                    "Failed to create shipment event for transport Document:"
                        + transportDocumentTO.getTransportDocumentReference())));
  }

  Mono<ShipmentEvent> shipmentEventFromTransportDocumentTO(
      TransportDocumentTO transportDocumentTO) {
    ShipmentEvent shipmentEvent = new ShipmentEvent();
    shipmentEvent.setShipmentEventTypeCode(
        ShipmentEventTypeCode.valueOf(
            transportDocumentTO.getShippingInstruction().getDocumentStatus().name()));
    shipmentEvent.setEventType(null);
    shipmentEvent.setCarrierBookingReference(null);
    shipmentEvent.setDocumentID(transportDocumentTO.getTransportDocumentReference());
    shipmentEvent.setEventDateTime(OffsetDateTime.now());
    shipmentEvent.setEventCreatedDateTime(OffsetDateTime.now());
    shipmentEvent.setEventClassifierCode(EventClassifierCode.PLN);
    shipmentEvent.setDocumentTypeCode(DocumentTypeCode.OOG);
    return Mono.just(shipmentEvent);
  }
}
