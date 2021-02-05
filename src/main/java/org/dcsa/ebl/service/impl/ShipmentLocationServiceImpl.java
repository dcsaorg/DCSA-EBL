package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.exception.UpdateException;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.dcsa.ebl.model.ShipmentLocation;
import org.dcsa.ebl.model.ShipmentLocationTO;
import org.dcsa.ebl.model.enums.ShipmentLocationType;
import org.dcsa.ebl.model.utils.MappingUtil;
import org.dcsa.ebl.repository.ShipmentLocationRepository;
import org.dcsa.ebl.service.ShipmentLocationService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.function.BiFunction;

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
        return shipmentLocationRepository.findByLocationTypeAndLocationIDAndShipmentIDIn(
                update.getLocationType(),
                update.getLocationID(),
                Collections.singletonList(update.getShipmentID())
        ).single()  // There must be exactly one match.
         .onErrorMap(NoSuchElementException.class, error -> new UpdateException("Cannot create ShipmentLocation via update ("
                 + update.getLocationType() + ", " + update.getLocationID() + ")"))
         .flatMap(current -> this.preUpdateHook(current, update))
         .flatMap(this::save);
    }

    public Mono<Void> updateAllRelatedFromTO(
            List<UUID> shipmentIDs,
            Iterable<ShipmentLocationTO> shipmentLocationTOs,
            BiFunction<ShipmentLocation, ShipmentLocationTO, Mono<ShipmentLocation>> mutator) {
        return Flux.fromIterable(shipmentLocationTOs)
                // FIXME: 1-N performance issue.  Slightly mitigated by shipmentIDs but it is still one
                // query per "location ID, location Type".
                .concatMap(shipmentLocationTO ->
                        Mono.zip(
                                Mono.just(shipmentLocationTO),
                                shipmentLocationRepository.findByLocationTypeAndLocationIDAndShipmentIDIn(
                                        shipmentLocationTO.getLocationType(),
                                        shipmentLocationTO.getLocationID(),
                                        shipmentIDs
                                ).collectList()
                        )
                ).concatMap(tuple -> {
                    ShipmentLocationTO shipmentLocationTO = tuple.getT1();
                    List<ShipmentLocation> originals = tuple.getT2();
                    if (originals.size() != shipmentIDs.size()) {
                        return Flux.error(new IllegalStateException("Got " + originals.size()
                                + " ShipmentLocations but expected " + shipmentIDs.size() + " for location type "
                                + shipmentLocationTO.getLocationType() + " and location id "
                                + shipmentLocationTO.getLocationID()));
                    }

                    return Flux.fromIterable(originals).concatMap(original -> {
                        ShipmentLocation update = MappingUtil.instanceFrom(original, ShipmentLocation::new, ShipmentLocation.class);
                        return Mono.zip(
                                Mono.just(original),
                                mutator.apply(update, shipmentLocationTO)
                        );
                    });
                })
                .concatMap(tuple -> preUpdateHook(tuple.getT1(), tuple.getT2()))
                .concatMap(this::preSaveHook)
                .buffer(SQL_LIST_BUFFER_SIZE)
                .concatMap(shipmentLocationRepository::saveAll)
                .then();
    }

    @Override
    public Flux<ShipmentLocation> createAll(Flux<ShipmentLocation> shipmentLocations) {
        return shipmentLocations
                .concatMap(this::preCreateHook)
                .concatMap(this::preSaveHook)
                .buffer(SQL_LIST_BUFFER_SIZE)
                .concatMap(shipmentLocationRepository::saveAll);
    }

    @Override
    public Flux<ShipmentLocation> findAllByShipmentIDIn(List<UUID> shipmentIDs) {
        return shipmentLocationRepository.findAllByShipmentIDIn(shipmentIDs);
    }

    @Override
    public Flux<ShipmentLocation> findByLocationTypeAndLocationIDAndShipmentIDIn(ShipmentLocationType shipmentLocationType, UUID locationID, List<UUID> shipmentIDs) {
        return shipmentLocationRepository.findByLocationTypeAndLocationIDAndShipmentIDIn(shipmentLocationType, locationID, shipmentIDs);
    }
}
