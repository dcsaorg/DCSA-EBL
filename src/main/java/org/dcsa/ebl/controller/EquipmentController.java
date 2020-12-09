package org.dcsa.ebl.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.dcsa.core.controller.ExtendedBaseController;
import org.dcsa.core.exception.DeleteException;
import org.dcsa.core.exception.UpdateException;
import org.dcsa.ebl.model.Equipment;
import org.dcsa.ebl.service.EquipmentService;
import org.springframework.http.HttpStatus;
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

    @GetMapping
    @Override
    public Flux<Equipment> findAll(ServerHttpResponse response, ServerHttpRequest request) {
        return super.findAll(response, request);
    }

    @GetMapping(path = "{equipmentReference}")
    @Override
    public Mono<Equipment> findById(@PathVariable String equipmentReference) {
        return super.findById(equipmentReference);
    }

    @Override
    @PostMapping
    public Mono<Equipment> create(@Valid @RequestBody Equipment equipment) {
        return super.create(equipment);
    }

    @PutMapping( path = "{equipmentReference}")
    @Override
    public Mono<Equipment> update(@PathVariable String equipmentReference, @Valid @RequestBody Equipment equipment) {
        return Mono.error(new UpdateException("Not possible to update an Equipment"));
    }

    @DeleteMapping
    @ResponseStatus( HttpStatus.NO_CONTENT )
    @Override
    public Mono<Void> delete(@RequestBody Equipment equipment) {
        return Mono.error(new DeleteException("Not possible to delete an Equipment"));
    }

    @DeleteMapping( path ="{equipmentReference}" )
    @ResponseStatus( HttpStatus.NO_CONTENT )
    @Override
    public Mono<Void> deleteById(@PathVariable String equipmentReference) {
        return Mono.error(new DeleteException("Not possible to delete an Equipment"));
    }
}
