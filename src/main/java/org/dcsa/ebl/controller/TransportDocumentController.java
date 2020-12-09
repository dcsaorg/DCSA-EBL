package org.dcsa.ebl.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.dcsa.core.controller.ExtendedBaseController;
import org.dcsa.core.exception.CreateException;
import org.dcsa.core.exception.DeleteException;
import org.dcsa.ebl.model.transferobjects.ChargeTO;
import org.dcsa.ebl.model.EBLEndorsementChain;
import org.dcsa.ebl.model.transferobjects.TransportDocumentTO;
import org.dcsa.ebl.service.TransportDocumentTOService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "transport-documents", produces = {MediaType.APPLICATION_JSON_VALUE})
@Tag(name = "Transport Documents", description = "The Transport Document API")
public class TransportDocumentController extends ExtendedBaseController<TransportDocumentTOService, TransportDocumentTO, UUID> {

    private final TransportDocumentTOService transportDocumentTOService;

    @Override
    public String getType() {
        return "TransportDocument";
    }

    @Override
    public TransportDocumentTOService getService() {
        return transportDocumentTOService;
    }

    @GetMapping
    @Override
    public Flux<TransportDocumentTO> findAll(ServerHttpResponse response, ServerHttpRequest request) {
        return super.findAll(response, request);
    }

    @GetMapping(path="{ID}")
    @Override
    public Mono<TransportDocumentTO> findById(@PathVariable UUID ID) {
        return super.findById(ID);
    }

    @PutMapping( path = "{ID}")
    @Override
    public Mono<TransportDocumentTO> update(@PathVariable UUID ID, @Valid @RequestBody TransportDocumentTO transportDocumentTO) {
        return super.update(ID, transportDocumentTO);
    }

    @PutMapping(path="{ID}/charges")
    public Flux<ChargeTO> updateCharges(@PathVariable UUID ID, @Valid @RequestBody List<ChargeTO> chargeList) {
        return getService().updateCharges(ID, chargeList);
    }

    @PutMapping(path="{ID}/ebl-endorsement-chain")
    public Flux<EBLEndorsementChain> updateEBLEndorsementChain(@PathVariable UUID ID, @Valid @RequestBody List<EBLEndorsementChain> eblEndorsementChainList) {
        return getService().updateEBLEndorsementChain(ID, eblEndorsementChainList);
    }


    @Override
    public Mono<TransportDocumentTO> create(@Valid @RequestBody TransportDocumentTO transportDocumentTO) {
        return Mono.error(new CreateException("Not possible to create a TransportDocument"));
    }

    @DeleteMapping
    @ResponseStatus( HttpStatus.NO_CONTENT )
    @Override
    public Mono<Void> delete(@RequestBody TransportDocumentTO transportDocumentTO) {
        return Mono.error(new DeleteException("Not possible to delete a TransportDocument"));
    }

    @DeleteMapping( path ="{ID}" )
    @ResponseStatus( HttpStatus.NO_CONTENT )
    @Override
    public Mono<Void> deleteById(@PathVariable UUID ID) {
        return Mono.error(new DeleteException("Not possible to delete a TransportDocument"));
    }
}
