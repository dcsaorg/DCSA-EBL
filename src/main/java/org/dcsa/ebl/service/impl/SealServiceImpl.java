package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.dcsa.ebl.model.Seal;
import org.dcsa.ebl.repository.SealRepository;
import org.dcsa.ebl.service.SealService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class SealServiceImpl extends ExtendedBaseServiceImpl<SealRepository, Seal, UUID> implements SealService {
    private final SealRepository sealRepository;

    @Override
    public SealRepository getRepository() {
        return sealRepository;
    }

    @Override
    public Flux<Seal> findAllByShipmentEquipmentID(UUID shipmentEquipmentID) {
        return sealRepository.findAllByShipmentEquipmentID(shipmentEquipmentID);
    }

}
