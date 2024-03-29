package org.dcsa.ebl.controller;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.events.model.transferobjects.ShippingInstructionTO;
import org.dcsa.core.exception.ConcreteRequestErrorMessageException;
import org.dcsa.ebl.model.transferobjects.ShippingInstructionResponseTO;
import org.dcsa.ebl.service.ShippingInstructionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping(
    value = "/shipping-instructions",
    produces = {MediaType.APPLICATION_JSON_VALUE})
public class ShippingInstructionController {

  private final ShippingInstructionService shippingInstructionService;

  @GetMapping(path = "/{shippingInstructionReference}")
  public Mono<ShippingInstructionTO> findByReference(
      @PathVariable String shippingInstructionReference) {
    return shippingInstructionService
        .findByReference(shippingInstructionReference)
        .switchIfEmpty(
            Mono.error(
                ConcreteRequestErrorMessageException.internalServerError(
                    "ShippingInstructionController.findById is attempting to return an empty Mono.")));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<ShippingInstructionResponseTO> create(
      @Valid @RequestBody ShippingInstructionTO shippingInstructionTO) {
    return shippingInstructionService.createShippingInstruction(shippingInstructionTO);
  }

  @PutMapping(path = "/{shippingInstructionReference}")
  @ResponseStatus(HttpStatus.OK)
  public Mono<ShippingInstructionResponseTO> update(
      @PathVariable String shippingInstructionReference,
      @Valid @RequestBody ShippingInstructionTO shippingInstructionTO) {
    return shippingInstructionService.updateShippingInstructionByShippingInstructionReference(
        shippingInstructionReference, shippingInstructionTO);
  }
}
