package org.dcsa.ebl.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.dcsa.core.controller.ExtendedBaseController;
import org.dcsa.core.exception.DeleteException;
import org.dcsa.ebl.model.transferobjects.*;
import org.dcsa.ebl.service.ShippingInstructionTOService;
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
@RequestMapping(value = "shipping-instructions", produces = {MediaType.APPLICATION_JSON_VALUE})
@Tag(name = "Shipping Instructions", description = "The Shipping Instruction API")
public class ShippingInstructionController extends ExtendedBaseController<ShippingInstructionTOService, ShippingInstructionTO, UUID> {

    private final ShippingInstructionTOService shippingInstructionTOService;

    @Override
    public ShippingInstructionTOService getService() {
        return shippingInstructionTOService;
    }

    @Override
    public String getType() {
        return "ShippingInstruction";
    }

    @GetMapping
    @Override
    public Flux<ShippingInstructionTO> findAll(ServerHttpResponse response, ServerHttpRequest request) {
        return super.findAll(response, request);
    }

    @GetMapping(path = "{shippingInstructionID}")
    @Override
    public Mono<ShippingInstructionTO> findById(@PathVariable UUID shippingInstructionID) {
        return super.findById(shippingInstructionID);
    }

    @Override
    @PostMapping
    public Mono<ShippingInstructionTO> create(@Valid @RequestBody ShippingInstructionTO shippingInstructionTO) {
        return super.create(shippingInstructionTO);
    }

    @PutMapping( path = "{shippingInstructionID}")
    @Override
    public Mono<ShippingInstructionTO> update(@PathVariable UUID shippingInstructionID, @Valid @RequestBody ShippingInstructionTO shippingInstructionTO) {
        return super.update(shippingInstructionID, shippingInstructionTO);
    }

    @PutMapping( path = "{shippingInstructionID}/stuffing")
    public Flux<StuffingTO> updateStuffing(@PathVariable UUID shippingInstructionID, @Valid @RequestBody List<StuffingTO> stuffingList) {
        return getService().updateStuffing(shippingInstructionID, stuffingList);
    }

    @PutMapping( path = "{shippingInstructionID}/equipments")
    public Flux<EquipmentTO> updateEquipments(@PathVariable UUID shippingInstructionID, @Valid @RequestBody List<EquipmentTO> equipmentList) {
        return getService().updateEquipments(shippingInstructionID, equipmentList);
    }

    @PutMapping( path = "{shippingInstructionID}/cargo-items")
    public Flux<CargoItemTO> updateCargoItems(@PathVariable UUID shippingInstructionID, @Valid @RequestBody List<CargoItemTO> cargoItemList) {
        return getService().updateCargoItems(shippingInstructionID, cargoItemList);
    }

    @PutMapping( path = "{shippingInstructionID}/parties")
    public Flux<DocumentPartyTO> updateParties(@PathVariable UUID shippingInstructionID, @Valid @RequestBody List<DocumentPartyTO> partyList) {
        return getService().updateParties(shippingInstructionID, partyList);
    }

    @DeleteMapping
    @ResponseStatus( HttpStatus.NO_CONTENT )
    @Override
    public Mono<Void> delete(@RequestBody ShippingInstructionTO shippingInstructionTO) {
        return Mono.error(new DeleteException("Not possible to delete a ShippingInstruction"));
    }

    @DeleteMapping( path ="{shippingInstructionID}" )
    @ResponseStatus( HttpStatus.NO_CONTENT )
    @Override
    public Mono<Void> deleteById(@PathVariable UUID shippingInstructionID) {
        return Mono.error(new DeleteException("Not possible to delete a ShippingInstruction"));
    }
}
