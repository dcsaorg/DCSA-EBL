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
import org.dcsa.ebl.model.ShippingInstruction;
import org.dcsa.ebl.service.ShippingInstructionService;
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
@RequestMapping(value = "shipping-instructions", produces = {MediaType.APPLICATION_JSON_VALUE})
@Tag(name = "Shipping Instructions", description = "The Shipping Instruction API")
public class ShippingInstructionController extends ExtendedBaseController<ShippingInstructionService, ShippingInstruction, UUID> {

    private final ShippingInstructionService shippingInstructionService;

    @Override
    public ShippingInstructionService getService() {
        return shippingInstructionService;
    }

    @Override
    public String getType() {
        return "ShippingInstruction";
    }

    @Operation(summary = "Find all Shipping Instructions", description = "Finds all Shipping Instructions in the database", tags = { "Shipping Instruction" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ShippingInstruction.class))))
    })
    @GetMapping
    @Override
    public Flux<ShippingInstruction> findAll(ServerHttpResponse response, ServerHttpRequest request) {
        return super.findAll(response, request);
    }

    @Operation(summary = "Create a Shipping Instruction", description = "Create a Shipping Instruction", tags = { "Shipping Instruction" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ShippingInstruction.class))))
    })
    @PostMapping( consumes = "application/json", produces = "application/json")
    @Override
    public Mono<ShippingInstruction> create(@Valid @RequestBody ShippingInstruction shippingInstruction) {
        return super.create(shippingInstruction);
    }
}
