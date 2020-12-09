package org.dcsa.ebl.service;

import org.dcsa.ebl.model.transferobjects.ShippingInstructionTO;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ShippingInstructionTOService {

    Mono<ShippingInstructionTO> create(ShippingInstructionTO shippingInstructionTO);
    Mono<ShippingInstructionTO> findById(UUID uuid);
}
