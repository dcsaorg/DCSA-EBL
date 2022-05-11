package org.dcsa.ebl.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dcsa.core.exception.ConcreteRequestErrorMessageException;
import org.dcsa.ebl.model.transferobjects.ApproveTransportDocumentRequestTO;
import org.dcsa.ebl.model.transferobjects.TransportDocumentRefStatusTO;
import org.dcsa.ebl.model.transferobjects.TransportDocumentTO;
import org.dcsa.ebl.service.TransportDocumentService;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import javax.validation.constraints.Size;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(
    value = "transport-documents",
    produces = {MediaType.APPLICATION_JSON_VALUE})
@Validated
public class TransportDocumentController {

  private final TransportDocumentService transportDocumentService;

  @GetMapping(path = "/{transportDocumentReference}")
  public Mono<TransportDocumentTO> findById(
      @PathVariable @Size(max = 20) String transportDocumentReference) {
    return transportDocumentService
        .findByTransportDocumentReference(transportDocumentReference)
        .switchIfEmpty(
            Mono.error(
                ConcreteRequestErrorMessageException.notFound(
                    "No transport document found with transport document reference: "
                        + transportDocumentReference)));
  }

  @PatchMapping(path = "{transportDocumentReference}")
  public Mono<TransportDocumentRefStatusTO> approveTransportDocumentReference(
      @PathVariable String transportDocumentReference,
      @RequestBody @Valid ApproveTransportDocumentRequestTO approveTransportDocumentRequestTO) {
    return transportDocumentService.approveTransportDocument(transportDocumentReference);
  }
}
