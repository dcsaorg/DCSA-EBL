package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.dcsa.ebl.model.ShippingInstruction;
import org.dcsa.ebl.repository.ShippingInstructionRepository;
import org.dcsa.ebl.service.ShippingInstructionService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ShippingInstructionServiceImpl extends ExtendedBaseServiceImpl<ShippingInstructionRepository, ShippingInstruction, String> implements ShippingInstructionService {

    private final ShippingInstructionRepository shippingInstructionRepository;

    @Override
    public ShippingInstructionRepository getRepository() {
        return shippingInstructionRepository;
    }

}
