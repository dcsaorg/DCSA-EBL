package org.dcsa.ebl.service;

import org.dcsa.core.events.model.transferobjects.ShippingInstructionTO;
import org.dcsa.ebl.model.transferobjects.ShippingInstructionResponseTO;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ShippingInstructionService {
  Mono<ShippingInstructionTO> findByReference(String shippingInstructionReference);

  Mono<ShippingInstructionTO> findByID(UUID shippingInstructionID);

  Mono<ShippingInstructionResponseTO> createShippingInstruction(
      ShippingInstructionTO shippingInstructionTO);

  Mono<ShippingInstructionResponseTO> updateShippingInstructionByShippingInstructionReference(
      String shippingInstructionReference, ShippingInstructionTO update);
}
