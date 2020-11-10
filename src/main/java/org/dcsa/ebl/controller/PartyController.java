package org.dcsa.ebl.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.dcsa.core.controller.ExtendedBaseController;
import org.dcsa.ebl.model.Party;
import org.dcsa.ebl.service.PartyService;
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

    @Operation(summary = "Find all Parties", description = "Finds all Parties in the database", tags = { "Party" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Party.class))))
    })
    @GetMapping
    @Override
    public Flux<Party> findAll(ServerHttpResponse response, ServerHttpRequest request) {
        return super.findAll(response, request);
    }

    @Operation(summary = "Find a Party", description = "Finds a specific Party in the database", tags = { "Party" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Party.class))))
    })
    @GetMapping
    @Override
    public Mono<Party> findById(UUID id) {
        return super.findById(id);
    }

    @Operation(summary = "Update a Party", description = "Update a Party", tags = { "Party" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Party.class))))
    })
    @PutMapping( consumes = "application/json", produces = "application/json")
    @Override
    public Mono<Party> update(UUID id, @Valid @RequestBody Party party) {
        return super.update(id, party);
    }
}
