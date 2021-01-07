package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.exception.UpdateException;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.dcsa.ebl.model.ShipmentLocation;
import org.dcsa.ebl.model.enums.ShipmentLocationType;
import org.dcsa.ebl.repository.ShipmentLocationRepository;
import org.dcsa.ebl.service.ShipmentLocationService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static org.dcsa.ebl.Util.SQL_LIST_BUFFER_SIZE;

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

    protected Mono<ShipmentLocation> preUpdateHook(ShipmentLocation current, ShipmentLocation update) {
        // FIXME: Revise this when we get compound Id support figured out
        // NB: We rely on this control check in the updateAll method
        if (! current.getShipmentID().equals(update.getShipmentID())) {
            return Mono.error(new UpdateException("update called with a non-matching item!"));
        }
        if (! current.getLocationID().equals(update.getLocationID())) {
            return Mono.error(new UpdateException("update called with a non-matching item!"));
        }
        if (! current.getLocationType().equals(update.getLocationType())) {
            return Mono.error(new UpdateException("update called with a non-matching item!"));
        }
        update.setId(current.getId());
        return super.preUpdateHook(current, update);
    }

    @Override
    public Mono<ShipmentLocation> update(ShipmentLocation update) {
        return shipmentLocationRepository.findByShipmentIDAndLocationTypeAndLocationID(update.getShipmentID(),
                update.getLocationType(),
                update.getLocationID()
        ).flatMap(current -> this.preUpdateHook(current, update))
         .flatMap(this::save);
    }

    @Override
    public Flux<ShipmentLocation> createAll(Flux<ShipmentLocation> shipmentLocations) {
        return shipmentLocations
                .concatMap(this::preCreateHook)
                .buffer(SQL_LIST_BUFFER_SIZE)
                .concatMap(shipmentLocationRepository::saveAll);
    }

    @Override
    public Flux<ShipmentLocation> findAllByShipmentIDIn(List<UUID> shipmentIDs) {
        return shipmentLocationRepository.findAllByShipmentIDIn(shipmentIDs);
    }

    @Override
    public Mono<ShipmentLocation> findByShipmentIDAndLocationTypeAndLocationID(UUID shipmentID, ShipmentLocationType shipmentLocationType, UUID locationID) {
        return shipmentLocationRepository.findByShipmentIDAndLocationTypeAndLocationID(shipmentID, shipmentLocationType, locationID);
    }
}
