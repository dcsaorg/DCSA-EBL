package org.dcsa.ebl.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.dcsa.core.exception.GetException;
import org.dcsa.core.extendedrequest.ExtendedParameters;
import org.dcsa.core.extendedrequest.ExtendedRequest;
import org.dcsa.ebl.extendedrequest.TransportDocumentExtendedRequest;
import org.dcsa.ebl.model.TransportDocument;
import org.dcsa.ebl.model.transferobjects.TransportDocumentTO;
import org.dcsa.ebl.service.TransportDocumentTOService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "transport-documents", produces = {MediaType.APPLICATION_JSON_VALUE})
@Tag(name = "Transport Documents", description = "The Transport Document API")
public class TransportDocumentController extends AbstractTOController<TransportDocumentTOService> {

    private final ExtendedParameters extendedParameters;

    private final TransportDocumentTOService transportDocumentTOService;

    @Override
    public TransportDocumentTOService getService() {
        return transportDocumentTOService;
    }

    @Override
    public String getType() {
        return "TransportDocument";
    }

    @GetMapping
    public Flux<TransportDocument> findAll(ServerHttpResponse response, ServerHttpRequest request) {
        ExtendedRequest<TransportDocument> extendedRequest = new TransportDocumentExtendedRequest<>(extendedParameters,
                TransportDocument.class);

        try {
            Map<String, String> params = request.getQueryParams().toSingleValueMap();
            extendedRequest.parseParameter(params);
        } catch (GetException e) {
            return Flux.error(e);
        }

        return transportDocumentTOService.findAllExtended(extendedRequest).doOnComplete(
                () -> extendedRequest.insertHeaders(response, request)
        );
    }

    @GetMapping(path="{transportDocumentID}")
    public Mono<TransportDocumentTO> findById(@PathVariable UUID transportDocumentID, @RequestParam(defaultValue = "false") boolean displayCharges) {
        return transportDocumentTOService.findById(transportDocumentID, displayCharges);
    }

    @PutMapping( path = "{transportDocumentID}")
    @ResponseStatus( HttpStatus.FORBIDDEN )
    public Mono<TransportDocumentTO> update(@PathVariable UUID transportDocumentID, @Valid @RequestBody TransportDocumentTO transportDocumentTO) {
        return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN));
    }

    @PostMapping
    @ResponseStatus( HttpStatus.CREATED )
    public Mono<TransportDocumentTO> create(@Valid @RequestBody TransportDocumentTO transportDocumentTO) {
        return transportDocumentTOService.create(transportDocumentTO);
    }

    @DeleteMapping
    @ResponseStatus( HttpStatus.FORBIDDEN )
    public Mono<Void> delete(@RequestBody TransportDocumentTO transportDocumentTO) {
        return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN));
    }

    @DeleteMapping( path ="{transportDocumentID}" )
    @ResponseStatus( HttpStatus.FORBIDDEN )
    public Mono<Void> deleteById(@PathVariable UUID transportDocumentID) {
        return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN));
    }
}
