package org.dcsa.ebl.service;

import org.dcsa.core.events.model.ShipmentLocation;
import org.dcsa.core.service.ExtendedBaseService;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface ShipmentLocationService extends ExtendedBaseService<ShipmentLocation, UUID> {
    Flux<ShipmentLocation> findAllByCarrierBookingReference(String carrierBookingReference);
}
