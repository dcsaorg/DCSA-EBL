package org.dcsa.ebl.controller;

import lombok.RequiredArgsConstructor;
import org.dcsa.ebl.model.transferobjects.TransportDocumentTO;
import org.dcsa.ebl.service.TransportDocumentService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "transport-documents", produces = {MediaType.APPLICATION_JSON_VALUE})
public class TransportDocumentController {

    private final TransportDocumentService transportDocumentService;

    @GetMapping(path="{transportDocumentReference}")
    public Mono<TransportDocumentTO> findById(@PathVariable String transportDocumentReference) {
        return transportDocumentService.findById(transportDocumentReference);
    }
}
