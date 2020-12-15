package org.dcsa.ebl.service;

import org.dcsa.core.service.ExtendedBaseService;
import org.dcsa.ebl.model.Seal;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface SealService extends ExtendedBaseService<Seal, UUID> {
    Flux<Seal> findAllByShipmentEquipmentID(UUID shipmentEquipmentID);
}
