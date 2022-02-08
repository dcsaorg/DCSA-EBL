package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.events.model.Seal;
import org.dcsa.core.events.repository.SealRepository;
import org.dcsa.ebl.service.SealService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class SealServiceImpl implements SealService {
    private final SealRepository sealRepository;

    public SealRepository getRepository() {
        return sealRepository;
    }

    @Override
    public Flux<Seal> findAllByShipmentEquipmentID(UUID shipmentEquipmentID) {
        return sealRepository.findAllByShipmentEquipmentID(shipmentEquipmentID);
    }

}
