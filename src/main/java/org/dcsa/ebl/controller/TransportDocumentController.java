package org.dcsa.ebl.controller;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.events.model.TransportDocument;
import org.dcsa.core.exception.GetException;
import org.dcsa.core.extendedrequest.ExtendedParameters;
import org.dcsa.core.extendedrequest.ExtendedRequest;
import org.dcsa.ebl.extendedrequest.TransportDocumentExtendedRequest;
import org.dcsa.ebl.model.transferobjects.TransportDocumentTO;
import org.dcsa.ebl.service.TransportDocumentTOService;
import org.springframework.data.r2dbc.dialect.R2dbcDialect;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "transport-documents", produces = {MediaType.APPLICATION_JSON_VALUE})
public class TransportDocumentController {

    private final ExtendedParameters extendedParameters;

    private final TransportDocumentTOService transportDocumentTOService;

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

        return transportDocumentTOService.findAllExtended(extendedRequest).doOnComplete(
                () -> extendedRequest.insertHeaders(response, request)
        );
    }

    @GetMapping(path="{transportDocumentReference}")
    public Mono<TransportDocumentTO> findById(@PathVariable String transportDocumentReference) {
        return transportDocumentTOService.findById(transportDocumentReference);
    }
}
