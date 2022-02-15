package org.dcsa.ebl.controller;

import lombok.RequiredArgsConstructor;
import org.dcsa.ebl.model.transferobjects.ShippingInstructionResponseTO;
import org.dcsa.ebl.model.transferobjects.ShippingInstructionTO;
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

  @GetMapping(path = "/{shippingInstructionID}")
  public Mono<ShippingInstructionTO> findById(@PathVariable String shippingInstructionID) {
    return shippingInstructionService.findById(shippingInstructionID);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<ShippingInstructionResponseTO> create(@Valid @RequestBody ShippingInstructionTO shippingInstructionTO) {
    return shippingInstructionService.createShippingInstruction(shippingInstructionTO);
  }

  @PutMapping(path = "/{shippingInstructionID}")
  public Mono<ShippingInstructionResponseTO> update(
      @PathVariable String shippingInstructionID,
      @Valid @RequestBody ShippingInstructionTO shippingInstructionTO) {
    return shippingInstructionService.updateShippingInstructionByShippingInstructionID(shippingInstructionID, shippingInstructionTO);
  }
}
