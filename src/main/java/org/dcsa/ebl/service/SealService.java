package org.dcsa.ebl.service;

import org.dcsa.core.events.model.Seal;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface SealService {
    Flux<Seal> findAllByShipmentEquipmentID(UUID shipmentEquipmentID);
}
