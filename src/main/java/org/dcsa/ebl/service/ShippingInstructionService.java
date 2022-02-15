package org.dcsa.ebl.service;

import org.dcsa.ebl.model.transferobjects.ShippingInstructionResponseTO;
import org.dcsa.ebl.model.transferobjects.ShippingInstructionTO;
import reactor.core.publisher.Mono;

public interface ShippingInstructionService {
  Mono<ShippingInstructionTO> findById(String shippingInstructionID);

  Mono<ShippingInstructionResponseTO> createShippingInstruction(ShippingInstructionTO shippingInstructionTO);

  Mono<ShippingInstructionResponseTO> updateShippingInstructionByShippingInstructionID(String shippingInstructionID, ShippingInstructionTO update);
}
