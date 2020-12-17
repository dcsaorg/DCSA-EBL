package org.dcsa.ebl.repository;

import org.dcsa.core.repository.ExtendedRepository;
import org.dcsa.ebl.model.ShipmentLocation;
import org.dcsa.ebl.model.enums.ShipmentLocationType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface ShipmentLocationRepository extends ExtendedRepository<ShipmentLocation, UUID> {

    Flux<ShipmentLocation> findAllByShipmentIDIn(List<UUID> shipmentIDs);

    Mono<ShipmentLocation> findByShipmentIDAndLocationTypeAndLocationID(UUID shipmentID,
                                                                        ShipmentLocationType shipmentLocationType,
                                                                        UUID locationID
                                                                        );
}
