package org.dcsa.ebl.service;

import org.dcsa.core.events.model.ShipmentTransport;
import org.dcsa.core.service.ExtendedBaseService;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface ShipmentTransportService extends ExtendedBaseService<ShipmentTransport, UUID> {
    Flux<ShipmentTransport> findByShipmentIDOrderBySequenceNumber(UUID shipmentID);
}
