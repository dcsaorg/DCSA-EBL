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
import org.dcsa.ebl.model.CargoItem;
import org.dcsa.ebl.service.CargoItemService;
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
@RequestMapping(value = "cargo-items", produces = {MediaType.APPLICATION_JSON_VALUE})
@Tag(name = "CargoItems", description = "The CargoItem API")
public class CargoItemController extends ExtendedBaseController<CargoItemService, CargoItem, UUID> {

    private final CargoItemService shipmentService;

    @Override
    public CargoItemService getService() {
        return shipmentService;
    }

    @Override
    public String getType() {
        return "CargoItem";
    }

    @Operation(summary = "Find all Cargo Items", description = "Finds all Cargo Items in the database", tags = { "Cargo Item" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CargoItem.class))))
    })
    @GetMapping
    @Override
    public Flux<CargoItem> findAll(ServerHttpResponse response, ServerHttpRequest request) {
        return super.findAll(response, request);
    }

    @Operation(summary = "Find a Cargo Item", description = "Finds a specific Cargo Item in the database", tags = { "Cargo Item" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CargoItem.class))))
    })
    @GetMapping
    @Override
    public Mono<CargoItem> findById(UUID id) {
        return super.findById(id);
    }

    @Operation(summary = "Update a Cargo Item", description = "Update a Cargo Item", tags = { "Cargo Item" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CargoItem.class))))
    })
    @PutMapping( consumes = "application/json", produces = "application/json")
    @Override
    public Mono<CargoItem> update(UUID id, @Valid @RequestBody CargoItem shipment) {
        return super.update(id, shipment);
    }
}
