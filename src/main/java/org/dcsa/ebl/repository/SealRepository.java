package org.dcsa.ebl.repository;

import org.dcsa.core.repository.ExtendedRepository;
import org.dcsa.ebl.model.Seal;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface SealRepository extends ExtendedRepository<Seal, UUID> {
    Flux<Seal> findAllByShipmentEquipmentID(UUID shipmentEquipmentID);
}
