package org.dcsa.ebl.service;

import org.dcsa.core.service.ExtendedBaseService;
import org.dcsa.ebl.model.ShippingInstruction;
import org.dcsa.ebl.model.transferobjects.ShippingInstructionTO;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ShippingInstructionService extends ExtendedBaseService<ShippingInstruction, UUID> {
    Mono<ShippingInstructionTO> createShippingInstructionTO(ShippingInstructionTO shippingInstructionTO);
}
