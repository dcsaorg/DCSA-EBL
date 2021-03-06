package org.dcsa.ebl.controller;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.extendedrequest.ExtendedParameters;
import org.dcsa.ebl.model.transferobjects.ShippingInstructionTO;
import org.dcsa.ebl.service.ShippingInstructionTOService;
import org.springframework.data.r2dbc.dialect.R2dbcDialect;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "shipping-instructions", produces = {MediaType.APPLICATION_JSON_VALUE})
public class ShippingInstructionTOController extends AbstractTOController<ShippingInstructionTOService> {

    private final ExtendedParameters extendedParameters;

    private final ShippingInstructionTOService shippingInstructionTOService;

    private final R2dbcDialect r2dbcDialect;

    @Override
    public ShippingInstructionTOService getService() {
        return shippingInstructionTOService;
    }

    @Override
    public String getType() {
        return "ShippingInstruction";
    }

    @GetMapping(path = "{shippingInstructionID}")
    public Mono<ShippingInstructionTO> findById(@PathVariable String shippingInstructionID) {
        return shippingInstructionTOService.findById(shippingInstructionID);
    }

    @Transactional
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Mono<ShippingInstructionTO> create(@Valid @RequestBody ShippingInstructionTO shippingInstructionTO) {
        return shippingInstructionTOService.create(shippingInstructionTO);
    }

    @PutMapping( path = "{shippingInstructionID}")
    public Mono<ShippingInstructionTO> update(@PathVariable String shippingInstructionID, @Valid @RequestBody ShippingInstructionTO shippingInstructionTO) {
        return shippingInstructionTOService.replaceOriginal(shippingInstructionID, shippingInstructionTO);
    }

    @DeleteMapping
    @ResponseStatus( HttpStatus.FORBIDDEN )
    public Mono<Void> delete(@RequestBody ShippingInstructionTO shippingInstructionTO) {
        return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN));
    }

    @DeleteMapping( path ="{shippingInstructionID}" )
    @ResponseStatus( HttpStatus.FORBIDDEN )
    public Mono<Void> deleteById(@PathVariable UUID shippingInstructionID) {
        return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN));
    }
}
