package org.dcsa.ebl.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.dcsa.core.controller.ExtendedBaseController;
import org.dcsa.ebl.model.TransportDocument;
import org.dcsa.ebl.service.TransportDocumentService;
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
@RequestMapping(value = "transport-documents", produces = {MediaType.APPLICATION_JSON_VALUE})
@Tag(name = "Transport Documents", description = "The Transport Document API")
public class TransportDocumentController extends ExtendedBaseController<TransportDocumentService, TransportDocument, UUID> {

    private final TransportDocumentService transportDocumentService;


    @Override
    public String getType() {
        return "TransportDocument";
    }

    @Override
    public TransportDocumentService getService() {
        return transportDocumentService;
    }

    @Operation(summary = "Find all Transport Documents", description = "Finds all Transport Documents in the database", tags = { "Transport Document" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TransportDocument.class))))
    })
    @GetMapping
    @Override
    public Flux<TransportDocument> findAll(ServerHttpResponse response, ServerHttpRequest request) {
        return super.findAll(response, request);
    }

    @Operation(summary = "Find Transport Document by ID", description = "Returns a single Transport Document", tags = { "Transport Document" }, parameters = {
            @Parameter(in = ParameterIn.PATH, name = "id", description="Id of the Transport Document to be obtained. Cannot be empty.", required=true),
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "404", description = "Transport Document not found")
    })
    @GetMapping(value="{id}", produces = "application/json")
    @Override
    public Mono<TransportDocument> findById(@PathVariable UUID id) {
        return super.findById(id);
    }

    @Operation(summary = "Creates a Transport Document", description = "Creates a Transport Document", tags = { "Transport Document" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation")
    })
    @PostMapping(consumes = "application/json", produces = "application/json")
    @Override
    public Mono<TransportDocument> create(@Valid @RequestBody TransportDocument transportDocument) {
        return super.create(transportDocument);
    }
}
