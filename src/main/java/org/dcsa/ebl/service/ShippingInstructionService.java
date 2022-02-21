package org.dcsa.ebl.service;

import org.dcsa.core.events.model.transferobjects.ShippingInstructionTO;
import org.dcsa.ebl.model.transferobjects.ShippingInstructionResponseTO;
import reactor.core.publisher.Mono;

public interface ShippingInstructionService {
  Mono<ShippingInstructionTO> findById(String shippingInstructionID);

  Mono<ShippingInstructionResponseTO> createShippingInstruction(ShippingInstructionTO shippingInstructionTO);

  Mono<ShippingInstructionResponseTO> updateShippingInstructionByShippingInstructionID(String shippingInstructionID, ShippingInstructionTO update);
}
