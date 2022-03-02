package org.dcsa.ebl.controller;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.exception.ConcreteRequestErrorMessageException;
import org.dcsa.ebl.model.transferobjects.TransportDocumentTO;
import org.dcsa.ebl.service.TransportDocumentService;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.validation.constraints.Size;

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
}
