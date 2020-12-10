package org.dcsa.ebl.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.dcsa.core.exception.DeleteException;
import org.dcsa.ebl.model.transferobjects.ShippingInstructionTO;
import org.dcsa.ebl.service.ShippingInstructionTOService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "shipping-instructions", produces = {MediaType.APPLICATION_JSON_VALUE})
@Tag(name = "Shipping Instructions", description = "The Shipping Instruction API")
public class ShippingInstructionTOController extends AbstractTOController<ShippingInstructionTOService> {

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
    public Flux<ShippingInstructionTO> findAll(ServerHttpResponse response, ServerHttpRequest request) {
        // FIXME
        //return shippingInstructionTOService.findAll(response, request);
        return Flux.empty();
    }

    @GetMapping(path = "{shippingInstructionID}")
    public Mono<ShippingInstructionTO> findById(@PathVariable UUID shippingInstructionID) {
        return shippingInstructionTOService.findById(shippingInstructionID);
    }

    @Transactional
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ShippingInstructionTO> create(@Valid @RequestBody ShippingInstructionTO shippingInstructionTO) {
        return shippingInstructionTOService.create(shippingInstructionTO);
    }

    @PutMapping( path = "{shippingInstructionID}")
    public Mono<ShippingInstructionTO> update(@PathVariable UUID shippingInstructionID, @Valid @RequestBody ShippingInstructionTO shippingInstructionTO) {
        // return shippingInstructionTOService.update(shippingInstructionID, shippingInstructionTO);
        return Mono.error(new UnsupportedOperationException("Not implemented yet"));
    }

    @DeleteMapping
    @ResponseStatus( HttpStatus.NO_CONTENT )
    public Mono<Void> delete(@RequestBody ShippingInstructionTO shippingInstructionTO) {
        return Mono.error(new DeleteException("Not possible to delete a ShippingInstruction"));
    }

    @DeleteMapping( path ="{shippingInstructionID}" )
    @ResponseStatus( HttpStatus.NO_CONTENT )
    public Mono<Void> deleteById(@PathVariable UUID shippingInstructionID) {
        return Mono.error(new DeleteException("Not possible to delete a ShippingInstruction"));
    }
}
