package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.events.edocumentation.model.transferobject.BookingTO;
import org.dcsa.core.events.edocumentation.service.ShipmentService;
import org.dcsa.core.events.model.Booking;
import org.dcsa.core.events.model.ShipmentEvent;
import org.dcsa.core.events.model.TransportDocument;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.core.events.repository.BookingRepository;
import org.dcsa.core.events.repository.TransportDocumentRepository;
import org.dcsa.core.events.service.ShipmentEventService;
import org.dcsa.core.exception.ConcreteRequestErrorMessageException;
import org.dcsa.core.exception.CreateException;
import org.dcsa.core.extendedrequest.ExtendedRequest;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.dcsa.ebl.model.transferobjects.ShippingInstructionTO;
import org.dcsa.ebl.model.transferobjects.TransportDocumentTO;
import org.dcsa.ebl.repository.ShippingInstructionRepository;
import org.dcsa.ebl.service.TransportDocumentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

@RequiredArgsConstructor
@Service
public class TransportDocumentServiceImpl extends ExtendedBaseServiceImpl<TransportDocumentRepository, TransportDocument, String> implements TransportDocumentService {


    private final BookingRepository bookingRepository;
    private final ShippingInstructionRepository shippingInstructionRepository;
    private final ShipmentService shipmentService;
    private final ShipmentEventService shipmentEventService;
    // Repositories
    private final TransportDocumentRepository transportDocumentRepository;


    public TransportDocumentRepository getRepository() {
        return transportDocumentRepository;
    }

    @Transactional
    @Override
    public Mono<TransportDocumentTO> findByTransportDocumentReference(String transportDocumentReference) {

        return Mono.empty();
    }

    public Flux<TransportDocument> findAllExtended(
            final ExtendedRequest<TransportDocument> extendedRequest) {
        return super.findAllExtended(extendedRequest);
    }

    public Mono<TransportDocumentTO> ApproveTransportDocument(String transportDocumentReference) {

        OffsetDateTime now = OffsetDateTime.now();

        return findByTransportDocumentReference(transportDocumentReference)
                .switchIfEmpty(
                        Mono.error(
                                ConcreteRequestErrorMessageException.invalidParameter(
                                        "No Transport Document found with ID: " + transportDocumentReference)))
                .flatMap(
                        TdTO -> {
                            if (TdTO.getShippingInstruction().getDocumentStatus() == ShipmentEventTypeCode.PENA
                                    || TdTO.getShippingInstruction().getDocumentStatus()
                                    == ShipmentEventTypeCode.PENU) {
                                return Mono.error(
                                        ConcreteRequestErrorMessageException.invalidParameter(
                                                "Cannot Approve Transport Document with Shipping Instruction that is not in status PENA"));
                            }

                            ShippingInstructionTO shippingInstructionTO = TdTO.getShippingInstruction();
                            shippingInstructionTO.setDocumentStatus(ShipmentEventTypeCode.APPR);
                            shippingInstructionTO.setShippingInstructionUpdatedDateTime(now);

                            return shipmentService
                                    .findByShippingInstructionID(TdTO.getShippingInstructionID())
                                    .doOnNext(
                                            shipmentTOs -> shipmentTOs.forEach(
                                                    shipmentTO ->
                                                            getBooking(
                                                                    shipmentTO.getCarrierBookingReference(),
                                                                    TdTO.getShippingInstructionID())
                                                                    .flatMap(
                                                                            b -> {
                                                                                BookingTO bookingTO = shipmentTO.getBooking();
                                                                                bookingTO.setDocumentStatus(ShipmentEventTypeCode.CMPL);
                                                                                bookingTO.setBookingRequestUpdatedDateTime(now);
                                                                                String carrierBookingRequestReference =
                                                                                        bookingTO.getCarrierBookingRequestReference();
                                                                                return bookingRepository
                                                                                        .updateDocumentStatusAndUpdatedDateTimeForCarrierBookingRequestReference(
                                                                                                ShipmentEventTypeCode.CMPL,
                                                                                                carrierBookingRequestReference,
                                                                                                now)
                                                                                        .thenReturn(bookingTO);
                                                                            })
                                                                    .doOnNext(shipmentTO::setBooking)
                                            ))
                                    .doOnNext(shippingInstructionTO::setShipments)
                                    .flatMap(
                                            ignored -> {
                                                shippingInstructionRepository.setDocumentStatusByID(
                                                        shippingInstructionTO.getDocumentStatus(),
                                                        shippingInstructionTO.getShippingInstructionUpdatedDateTime(),
                                                        shippingInstructionTO.getShippingInstructionID());
                                                TdTO.setShippingInstruction(shippingInstructionTO);
                                                return Mono.just(TdTO);
                                            })
                                    .flatMap(
                                            TdTO2 -> createShipmentEventFromTransportDocumentTO(TdTO2).thenReturn(TdTO2));
                        });
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

    private Mono<ShipmentEvent> createShipmentEventFromTransportDocumentTO(
            TransportDocumentTO transportDocumentTO) {

        return shipmentEventFromBooking(transportDocumentTO)
                .flatMap(shipmentEventService::create)
                .switchIfEmpty(
                        Mono.error(
                                new CreateException("Failed to create shipment event for transport Document.")));
    }

    private Mono<ShipmentEvent> shipmentEventFromBooking(TransportDocumentTO transportDocumentTO) {
        ShipmentEvent shipmentEvent = new ShipmentEvent();
        shipmentEvent.setShipmentEventTypeCode(
                ShipmentEventTypeCode.valueOf(transportDocumentTO.getShippingInstruction().getDocumentStatus().name()));
        shipmentEvent.setEventType(null);
        shipmentEvent.setCarrierBookingReference(null);
        shipmentEvent.setDocumentID(transportDocumentTO.getShippingInstructionID());
        shipmentEvent.setEventDateTime(OffsetDateTime.now());
        shipmentEvent.setEventCreatedDateTime(OffsetDateTime.now());
        return Mono.just(shipmentEvent);
    }
}
