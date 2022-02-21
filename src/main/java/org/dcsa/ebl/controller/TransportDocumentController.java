package org.dcsa.ebl.controller;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.events.model.TransportDocument;
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


@RestController
@RequiredArgsConstructor
@RequestMapping(value = "transport-documents", produces = {MediaType.APPLICATION_JSON_VALUE})
public class TransportDocumentController {

    private final ExtendedParameters extendedParameters;

    private final TransportDocumentService transportDocumentService;

    private final R2dbcDialect r2dbcDialect;

    @GetMapping
    public Flux<TransportDocument> findAll(ServerHttpResponse response, ServerHttpRequest request) {
        ExtendedRequest<TransportDocument> extendedRequest = new TransportDocumentExtendedRequest<>(extendedParameters,
                r2dbcDialect, TransportDocument.class);

        try {
            extendedRequest.parseParameter(request.getQueryParams());
        } catch (GetException e) {
            return Flux.error(e);
        }

        return transportDocumentService.findAllExtended(extendedRequest).doOnComplete(
                () -> extendedRequest.insertHeaders(response, request)
        );
    }

    @GetMapping(path="{transportDocumentReference}")
    public Mono<TransportDocumentTO> findById(@PathVariable String transportDocumentReference) {
        return transportDocumentService.findByTransportDocumentReference(transportDocumentReference);
    }

  @PutMapping(path = "{transportDocumentReference}")
  public Mono<TransportDocumentTO> updateTransportDocumentReference(
          @PathVariable String transportDocumentReference,
          @RequestBody @EnumSubset(anyOf = {"APPR"}) String documentStatus) { // Need shipmentEventTypeCode as class to validate

    return transportDocumentService.ApproveTransportDocument(transportDocumentReference);
  }

}
