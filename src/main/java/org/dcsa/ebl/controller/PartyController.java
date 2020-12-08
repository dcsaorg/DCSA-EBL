package org.dcsa.ebl.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.dcsa.core.controller.ExtendedBaseController;
import org.dcsa.core.exception.CreateException;
import org.dcsa.core.exception.DeleteException;
import org.dcsa.core.exception.UpdateException;
import org.dcsa.ebl.model.Party;
import org.dcsa.ebl.service.PartyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "parties", produces = {MediaType.APPLICATION_JSON_VALUE})
@Tag(name = "Parties", description = "The Party API")
public class PartyController extends ExtendedBaseController<PartyService, Party, UUID> {

    private final PartyService partyService;

    @Override
    public PartyService getService() {
        return partyService;
    }

    @Override
    public String getType() {
        return "Party";
    }

    @GetMapping
    @Override
    public Flux<Party> findAll(ServerHttpResponse response, ServerHttpRequest request) {
        return super.findAll(response, request);
    }

    @GetMapping(path = "{id}")
    @Override
    public Mono<Party> findById(@PathVariable UUID id) {
        return super.findById(id);
    }

    @PostMapping
    @Override
    public Mono<Party> create(@Valid @RequestBody Party party) {
        return Mono.error(new CreateException("Not possible to create a Party"));
    }

    @PutMapping( path = "{partyID}")
    @Override
    public Mono<Party> update(@PathVariable UUID partyID, @Valid @RequestBody Party party) {
        return Mono.error(new UpdateException("Not possible to update a Party"));
    }

    @DeleteMapping
    @ResponseStatus( HttpStatus.NO_CONTENT )
    @Override
    public Mono<Void> delete(@RequestBody Party party) {
        return Mono.error(new DeleteException("Not possible to delete a Party"));
    }

    @DeleteMapping( path ="{partyID}" )
    @ResponseStatus( HttpStatus.NO_CONTENT )
    @Override
    public Mono<Void> deleteById(@PathVariable UUID partyID) {
        return Mono.error(new DeleteException("Not possible to delete a Party"));
    }
}
