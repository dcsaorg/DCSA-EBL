package org.dcsa.ebl.controller;

import lombok.RequiredArgsConstructor;
import org.dcsa.ebl.model.transferobjects.ShippingInstructionResponseTO;
import org.dcsa.ebl.model.transferobjects.ShippingInstructionTO;
import org.dcsa.ebl.service.ShippingInstructionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping(
    value = "shipping-instructions",
    produces = {MediaType.APPLICATION_JSON_VALUE})
public class ShippingInstructionController {

  private final ShippingInstructionService shippingInstructionService;

  @GetMapping(path = "{shippingInstructionID}")
  public Mono<ShippingInstructionTO> findById(@PathVariable String shippingInstructionID) {
    return shippingInstructionService.findById(shippingInstructionID);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<ShippingInstructionResponseTO> create(@Valid @RequestBody ShippingInstructionTO shippingInstructionTO) {
    return shippingInstructionService.createShippingInstruction(shippingInstructionTO);
  }

  @PutMapping(path = "{shippingInstructionID}")
  public Mono<ShippingInstructionTO> update(
      @PathVariable String shippingInstructionID,
      @Valid @RequestBody ShippingInstructionTO shippingInstructionTO) {
    return shippingInstructionService.replaceOriginal(shippingInstructionID, shippingInstructionTO);
  }

  @DeleteMapping
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public Mono<Void> delete(@RequestBody ShippingInstructionTO shippingInstructionTO) {
    return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN));
  }

  @DeleteMapping(path = "{shippingInstructionID}")
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public Mono<Void> deleteById(@PathVariable UUID shippingInstructionID) {
    return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN));
  }
}
