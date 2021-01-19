package org.dcsa.ebl.service;

import org.dcsa.core.service.ExtendedBaseService;
import org.dcsa.ebl.model.ShipmentLocation;
import org.dcsa.ebl.model.ShipmentLocationTO;
import org.dcsa.ebl.model.enums.ShipmentLocationType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;

public interface ShipmentLocationService extends ExtendedBaseService<ShipmentLocation, UUID> {
    Flux<ShipmentLocation> createAll(Flux<ShipmentLocation> shipmentLocations);
    Flux<ShipmentLocation> findAllByShipmentIDIn(List<UUID> shipmentIDs);

    Mono<Void> updateAllRelatedFromTO(
            List<UUID> shipmentIDs,
            Iterable<ShipmentLocationTO> shipmentLocationTOs,
            BiFunction<ShipmentLocation, ShipmentLocationTO, Mono<ShipmentLocation>> mutator
    );

    Flux<ShipmentLocation> findByLocationTypeAndLocationIDAndShipmentIDIn(
                                                                          ShipmentLocationType shipmentLocationType,
                                                                          UUID locationID,
                                                                          List<UUID> shipmentID
    );
}
