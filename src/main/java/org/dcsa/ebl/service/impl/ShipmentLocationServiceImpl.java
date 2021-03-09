package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.dcsa.ebl.model.ShipmentLocation;
import org.dcsa.ebl.repository.ShipmentLocationRepository;
import org.dcsa.ebl.service.ShipmentLocationService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

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
    public Mono<ShipmentLocation> findById(UUID id) {
        return Mono.error(new UnsupportedOperationException("findById not supported"));
    }

    @Override
    public Mono<ShipmentLocation> update(ShipmentLocation update) {
        return Mono.error(new UnsupportedOperationException("update not supported"));
    }

    @Override
    public Flux<ShipmentLocation> findAllByCarrierBookingReference(String carrierBookingReference) {
        return shipmentLocationRepository.findAllByCarrierBookingReference(carrierBookingReference);
    }
}
