package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.dcsa.ebl.model.ShipmentLocation;
import org.dcsa.ebl.repository.ShipmentLocationRepository;
import org.dcsa.ebl.service.ShipmentLocationService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@RequiredArgsConstructor
@Service
public class ShipmentLocationServiceImpl extends ExtendedBaseServiceImpl<ShipmentLocationRepository, ShipmentLocation, UUID> implements ShipmentLocationService {
    private final ShipmentLocationRepository shipmentLocationRepository;

    @Override
    public ShipmentLocationRepository getRepository() {
        return shipmentLocationRepository;
    }

    @Override
    public Class<ShipmentLocation> getModelClass() {
        return ShipmentLocation.class;
    }

    @Override
    public Flux<ShipmentLocation> createAll(Flux<ShipmentLocation> shipmentLocations) {
        return shipmentLocations
                .concatMap(this::preCreateHook)
                .buffer(70)
                .concatMap(shipmentLocationRepository::saveAll);
    }

    @Override
    public Flux<ShipmentLocation> findAllByShipmentIDIn(List<UUID> shipmentIDs) {
        return shipmentLocationRepository.findAllByShipmentIDIn(shipmentIDs);
    }
}
