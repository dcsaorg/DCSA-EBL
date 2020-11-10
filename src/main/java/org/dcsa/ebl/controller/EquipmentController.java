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
import org.dcsa.ebl.model.Equipment;
import org.dcsa.ebl.service.EquipmentService;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "equipments", produces = {MediaType.APPLICATION_JSON_VALUE})
@Tag(name = "Equipments", description = "The Equipment API")
public class EquipmentController extends ExtendedBaseController<EquipmentService, Equipment, String> {

    private final EquipmentService equipmentService;

    @Override
    public EquipmentService getService() {
        return equipmentService;
    }

    @Override
    public String getType() {
        return "Equipment";
    }

    @Operation(summary = "Find all Equipments", description = "Finds all Equipments in the database", tags = { "Equipment" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Equipment.class))))
    })
    @GetMapping
    @Override
    public Flux<Equipment> findAll(ServerHttpResponse response, ServerHttpRequest request) {
        return super.findAll(response, request);
    }

    @Operation(summary = "Find a Equipment", description = "Finds a specific Equipment in the database", tags = { "Equipment" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Equipment.class))))
    })
    @GetMapping
    @Override
    public Mono<Equipment> findById(String equipmentReference) {
        return super.findById(equipmentReference);
    }

    @Operation(summary = "Update a Equipment", description = "Update a Equipment", tags = { "Equipment" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Equipment.class))))
    })
    @PutMapping( consumes = "application/json", produces = "application/json")
    @Override
    public Mono<Equipment> update(String equipmentReference, @Valid @RequestBody Equipment equipment) {
        return super.update(equipmentReference, equipment);
    }
}
