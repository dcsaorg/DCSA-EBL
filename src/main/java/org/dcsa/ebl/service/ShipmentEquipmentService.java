package org.dcsa.ebl.service;

import org.dcsa.core.service.ExtendedBaseService;
import org.dcsa.ebl.model.ShipmentEquipment;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

public interface ShipmentEquipmentService extends ExtendedBaseService<ShipmentEquipment, UUID> {
    Flux<ShipmentEquipment> findAllByShipmentIDIn(List<UUID> shipmentIDs);
}
