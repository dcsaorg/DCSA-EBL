package org.dcsa.ebl.repository;

import org.dcsa.core.repository.ExtendedRepository;
import org.dcsa.ebl.model.Location;
import org.dcsa.ebl.model.ShipmentLocation;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

public interface ShipmentLocationRepository extends ExtendedRepository<ShipmentLocation, UUID> {

    Flux<ShipmentLocation> findAllByShipmentIDIn(List<UUID> shipmentIDs);
}
