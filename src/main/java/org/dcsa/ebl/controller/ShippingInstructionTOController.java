package org.dcsa.ebl.controller;

import com.github.fge.jsonpatch.JsonPatch;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.dcsa.core.exception.DeleteException;
import org.dcsa.core.exception.GetException;
import org.dcsa.core.extendedrequest.ExtendedParameters;
import org.dcsa.core.extendedrequest.ExtendedRequest;
import org.dcsa.ebl.model.ShippingInstruction;
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
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "shipping-instructions", produces = {MediaType.APPLICATION_JSON_VALUE})
@Tag(name = "Shipping Instructions", description = "The Shipping Instruction API")
public class ShippingInstructionTOController extends AbstractTOController<ShippingInstructionTOService> {

    private final ExtendedParameters extendedParameters;

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
        ExtendedRequest<ShippingInstruction> extendedRequest = new ExtendedRequest<>(extendedParameters,
                ShippingInstruction.class);

        try {
            Map<String, String> params = request.getQueryParams().toSingleValueMap();
            extendedRequest.parseParameter(params);
        } catch (GetException e) {
            return Flux.error(e);
        }

        return shippingInstructionTOService.findAllExtended(extendedRequest).doOnComplete(
                () -> extendedRequest.insertHeaders(response, request)
        );
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

    @Transactional
    @PatchMapping(path = "{shippingInstructionID}", consumes = "application/json-patch+json")
    public Mono<ShippingInstructionTO> patch(@PathVariable UUID shippingInstructionID, @Valid @RequestBody JsonPatch patch) {
        return shippingInstructionTOService.patchOriginal(shippingInstructionID, patch);
    }

    @PutMapping( path = "{shippingInstructionID}")
    public Mono<ShippingInstructionTO> update(@PathVariable UUID shippingInstructionID, @Valid @RequestBody ShippingInstructionTO shippingInstructionTO) {
        return shippingInstructionTOService.replaceOriginal(shippingInstructionID, shippingInstructionTO);
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
