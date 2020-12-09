package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.dcsa.ebl.model.ShipmentTO;
import org.dcsa.ebl.repository.ShipmentTORepository;
import org.dcsa.ebl.service.ShipmentTOService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ShipmentTOServiceImpl extends ExtendedBaseServiceImpl<ShipmentTORepository, ShipmentTO, UUID> implements ShipmentTOService {
    private final ShipmentTORepository shipmentTORepository;

    @Override
    public ShipmentTORepository getRepository() {
        return shipmentTORepository;
    }

    @Override
    public Class<ShipmentTO> getModelClass() {
        return ShipmentTO.class;
    }
}
