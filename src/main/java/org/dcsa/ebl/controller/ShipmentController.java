package org.dcsa.ebl.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.dcsa.core.controller.ExtendedBaseController;
import org.dcsa.core.exception.CreateException;
import org.dcsa.core.exception.DeleteException;
import org.dcsa.core.exception.UpdateException;
import org.dcsa.ebl.model.transferobjects.ShipmentTO;
import org.dcsa.ebl.service.ShipmentTOService;
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
@RequestMapping(value = "shipments", produces = {MediaType.APPLICATION_JSON_VALUE})
@Tag(name = "Shipments", description = "The Shipment API")
public class ShipmentController extends ExtendedBaseController<ShipmentTOService, ShipmentTO, UUID> {

    private final ShipmentTOService shipmentTOService;

    @Override
    public ShipmentTOService getService() {
        return shipmentTOService;
    }

    @Override
    public String getType() {
        return "Shipment";
    }

    @GetMapping
    @Override
    public Flux<ShipmentTO> findAll(ServerHttpResponse response, ServerHttpRequest request) {
        return super.findAll(response, request);
    }

    @GetMapping(path = "{shipmentID}")
    @Override
    public Mono<ShipmentTO> findById(@PathVariable UUID shipmentID) {
        return super.findById(shipmentID);
    }


    @PostMapping
    @Override
    public Mono<ShipmentTO> create(@Valid @RequestBody ShipmentTO shipmentTO) {
        return Mono.error(new CreateException("Not possible to create a Shipment"));
    }

    @PutMapping( path = "{shipmentID}" )
    @Override
    public Mono<ShipmentTO> update(@PathVariable UUID shipmentID, @Valid @RequestBody ShipmentTO shipmentTO) {
        return Mono.error(new UpdateException("Not possible to update a Shipment"));
    }

    @DeleteMapping
    @ResponseStatus( HttpStatus.NO_CONTENT )
    @Override
    public Mono<Void> delete(@RequestBody ShipmentTO shipmentTO) {
        return Mono.error(new DeleteException("Not possible to delete a Shipment"));
    }

    @DeleteMapping( path = "{shipmentID}" )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Override
    public Mono<Void> deleteById(@PathVariable UUID shipmentID) {
        return Mono.error(new DeleteException("Not possible to delete a Shipment"));
    }
}
