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
import org.dcsa.core.exception.CreateException;
import org.dcsa.ebl.model.Shipment;
import org.dcsa.ebl.service.ShipmentService;
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
@RequestMapping(value = "shipments", produces = {MediaType.APPLICATION_JSON_VALUE})
@Tag(name = "Shipments", description = "The Shipment API")
public class ShipmentController extends ExtendedBaseController<ShipmentService, Shipment, UUID> {

    private final ShipmentService shipmentService;

    @Override
    public ShipmentService getService() {
        return shipmentService;
    }

    @Override
    public String getType() {
        return "Shipment";
    }

    @Operation(summary = "Find all Shipments", description = "Finds all Shipments in the database", tags = { "Shipment" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Shipment.class))))
    })
    @GetMapping
    @Override
    public Flux<Shipment> findAll(ServerHttpResponse response, ServerHttpRequest request) {
        return super.findAll(response, request);
    }

    @Operation(summary = "Find a Shipment", description = "Finds a specific Shipment in the database", tags = { "Shipment" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Shipment.class))))
    })
    @GetMapping(path = "{id}")
    @Override
    public Mono<Shipment> findById(@PathVariable UUID id) {
        return super.findById(id);
    }

    @Operation(summary = "Update a Shipment", description = "Update a Shipment", tags = { "Shipment" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Shipment.class))))
    })
    @PutMapping( path = "{id}", consumes = "application/json", produces = "application/json")
    @Override
    public Mono<Shipment> update(@PathVariable UUID id, @Valid @RequestBody Shipment shipment) {
        return super.update(id, shipment);
    }

    @Override
    public Mono<Shipment> create(@Valid @RequestBody Shipment shipment) {
        return Mono.error(new CreateException("Not possible to create a Shipment"));
    }
}
