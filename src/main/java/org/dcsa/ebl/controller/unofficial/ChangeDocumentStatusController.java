package org.dcsa.ebl.controller.unofficial;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.core.events.repository.BookingRepository;
import org.dcsa.core.events.repository.TransportDocumentRepository;
import org.dcsa.core.exception.ConcreteRequestErrorMessageException;
import org.dcsa.ebl.repository.ShippingInstructionRepository;
import org.dcsa.ebl.service.TransportDocumentService;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(
    value = "unofficial/change-document-status-by-transport-document",
    produces = {MediaType.APPLICATION_JSON_VALUE})
@Validated
public class ChangeDocumentStatusController {

  private final TransportDocumentService transportDocumentService;
  private final ShippingInstructionRepository shippingInstructionRepository;
  private final TransportDocumentRepository transportDocumentRepository;
  private final BookingRepository bookingRepository;

  @PostMapping(path = "/{transportDocumentId}")
  public Mono<Void> updateDocumentStatusForBookingAndShippingInstructionByTransportDocumentID(
      @PathVariable UUID transportDocumentId,
      @RequestParam(required = false) ShipmentEventTypeCode bookingStatus,
      @RequestParam(required = false) ShipmentEventTypeCode shippingInstructionStatus) {

    if (bookingStatus == null) bookingStatus = ShipmentEventTypeCode.CONF;
    if (shippingInstructionStatus == null) shippingInstructionStatus = ShipmentEventTypeCode.DRFT;

    ShipmentEventTypeCode finalBookingStatus = bookingStatus;
    ShipmentEventTypeCode finalShippingInstructionStatus = shippingInstructionStatus;

    return transportDocumentRepository
        .findById(transportDocumentId)
        .switchIfEmpty(
            Mono.error(
                ConcreteRequestErrorMessageException.notFound(
                    "No transport document found with transport document id: "
                        + transportDocumentId)))
        .flatMap(
            transportDocumentTO ->
                shippingInstructionRepository
                    .findById(transportDocumentTO.getShippingInstructionID())
                    .flatMap(
                        shippingInstruction -> {
                          shippingInstruction.setDocumentStatus(finalShippingInstructionStatus);
                          return shippingInstructionRepository.save(shippingInstruction);
                        }))
        .flatMap(
            shippingInstruction ->
                bookingRepository
                    .findAllByShippingInstructionReference(
                        shippingInstruction.getShippingInstructionReference())
                    .flatMap(
                        booking -> {
                          booking.setDocumentStatus(finalBookingStatus);
                          return bookingRepository.save(booking);
                        })
                    .then(Mono.empty()));
  }
}
