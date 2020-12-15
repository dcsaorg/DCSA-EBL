package org.dcsa.ebl.service;

import org.dcsa.core.service.ExtendedBaseService;
import org.dcsa.ebl.model.ActiveReeferSettings;
import org.dcsa.ebl.model.ShipmentEquipment;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ActiveReeferSettingsService extends ExtendedBaseService<ActiveReeferSettings, UUID> {
    Mono<ActiveReeferSettings> findByShipmentEquipmentID(UUID shipmentEquipmentID);
}
