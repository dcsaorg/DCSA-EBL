package org.dcsa.ebl.repository;

import org.dcsa.core.repository.ExtendedRepository;
import org.dcsa.ebl.model.ActiveReeferSettings;
import org.dcsa.ebl.model.ShipmentEquipment;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ActiveReeferSettingsRepository extends ExtendedRepository<ActiveReeferSettings, UUID> {
    Mono<ActiveReeferSettings> findByShipmentEquipmentID(UUID shipmentEquipmentID);
}
