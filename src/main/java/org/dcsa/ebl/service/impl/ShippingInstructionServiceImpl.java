package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.dcsa.ebl.model.ShippingInstruction;
import org.dcsa.ebl.model.transferobjects.ShippingInstructionTO;
import org.dcsa.ebl.repository.ShippingInstructionRepository;
import org.dcsa.ebl.service.ShippingInstructionService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ShippingInstructionServiceImpl extends ExtendedBaseServiceImpl<ShippingInstructionRepository, ShippingInstruction, UUID> implements ShippingInstructionService {
    private final ShippingInstructionRepository shippingInstructionRepository;


    @Override
    public ShippingInstructionRepository getRepository() {
        return shippingInstructionRepository;
    }

    @Override
    public Class<ShippingInstruction> getModelClass() {
        return ShippingInstruction.class;
    }

    public Mono<ShippingInstructionTO> createShippingInstructionTO(ShippingInstructionTO shippingInstructionTO) {
        return Mono.just(shippingInstructionTO);
    }
}
