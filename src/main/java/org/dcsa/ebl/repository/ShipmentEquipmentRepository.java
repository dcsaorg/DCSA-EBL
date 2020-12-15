package org.dcsa.ebl.repository;

import org.dcsa.core.repository.ExtendedRepository;
import org.dcsa.ebl.model.ShipmentEquipment;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

public interface ShipmentEquipmentRepository extends ExtendedRepository<ShipmentEquipment, UUID> {
    Flux<ShipmentEquipment> findAllByShipmentIDIn(List<UUID> shipmentIDs);
}
