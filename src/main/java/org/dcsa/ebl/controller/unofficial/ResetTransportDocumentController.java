package org.dcsa.ebl.controller.unofficial;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.core.events.repository.BookingRepository;
import org.dcsa.core.events.repository.TransportDocumentRepository;
import org.dcsa.core.exception.ConcreteRequestErrorMessageException;
import org.dcsa.ebl.model.transferobjects.ApproveTransportDocumentRequestTO;
import org.dcsa.ebl.model.transferobjects.TransportDocumentRefStatusTO;
import org.dcsa.ebl.model.transferobjects.TransportDocumentTO;
import org.dcsa.ebl.repository.ShippingInstructionRepository;
import org.dcsa.ebl.service.TransportDocumentService;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(
    value = "unofficial/reset-transport-document",
    produces = {MediaType.APPLICATION_JSON_VALUE})
@Validated
public class ResetTransportDocumentController {

  private final TransportDocumentService transportDocumentService;
  private final ShippingInstructionRepository shippingInstructionRepository;
  private final TransportDocumentRepository transportDocumentRepository;
  private final BookingRepository bookingRepository;

  @PostMapping(path = "/{transportDocumentId}")
  public Mono<Void> findById(@PathVariable UUID transportDocumentId) {
    System.out.println(transportDocumentId);
    return transportDocumentRepository
        .findById(transportDocumentId)
        .switchIfEmpty(Mono.error(ConcreteRequestErrorMessageException.notFound("argh!")))
        .flatMap(
            transportDocumentTO ->
                shippingInstructionRepository
                    .findById(transportDocumentTO.getShippingInstructionID())
                    .flatMap(
                        shippingInstruction -> {
                          shippingInstruction.setDocumentStatus(ShipmentEventTypeCode.DRFT);
                          return shippingInstructionRepository.save(shippingInstruction);
                        }))
        .flatMap(
            shippingInstruction ->
                bookingRepository
                    .findAllByShippingInstructionReference(
                        shippingInstruction.getShippingInstructionReference())
                    .flatMap(
                        booking -> {
                          System.out.println(booking.getId());
                          booking.setDocumentStatus(ShipmentEventTypeCode.CONF);
                          return bookingRepository.save(booking);
                        })
                    .collectList())
        .flatMap(transportDocumentTO -> Mono.empty());
  }
}
