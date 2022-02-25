package org.dcsa.ebl.controller;

import lombok.RequiredArgsConstructor;

import org.dcsa.core.exception.GetException;
import org.dcsa.core.extendedrequest.ExtendedParameters;
import org.dcsa.core.extendedrequest.ExtendedRequest;
import org.dcsa.core.validator.EnumSubset;
import org.dcsa.ebl.extendedrequest.TransportDocumentExtendedRequest;
import org.dcsa.ebl.model.transferobjects.TransportDocumentTO;
import org.dcsa.ebl.service.TransportDocumentService;
import org.springframework.data.r2dbc.dialect.R2dbcDialect;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


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


  @PutMapping(path = "{transportDocumentReference}")
  public Mono<TransportDocumentTO> updateTransportDocumentReference(
          @PathVariable String transportDocumentReference,
          @RequestBody @EnumSubset(anyOf = {"APPR"}) shipmentEventTypeCodeRequestTO documentStatus) {
        // Need shipmentEventTypeCodeRequestTO as class to validate RequestBody
    return transportDocumentService.ApproveTransportDocument(transportDocumentReference);
  }

}
