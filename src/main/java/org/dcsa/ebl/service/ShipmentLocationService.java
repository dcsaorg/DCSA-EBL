package org.dcsa.ebl.service;

import org.dcsa.core.service.ExtendedBaseService;
import org.dcsa.ebl.model.ShipmentLocation;
import org.dcsa.ebl.model.enums.ShipmentLocationType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface ShipmentLocationService extends ExtendedBaseService<ShipmentLocation, UUID> {
    Flux<ShipmentLocation> createAll(Flux<ShipmentLocation> shipmentLocations);
    Flux<ShipmentLocation> findAllByShipmentIDIn(List<UUID> shipmentIDs);

    Mono<ShipmentLocation> findByShipmentIDAndLocationTypeAndLocationID(UUID shipmentID,
                                                                        ShipmentLocationType shipmentLocationType,
                                                                        UUID locationID
    );
}
