package org.dcsa.ebl.service;

import org.dcsa.core.service.ExtendedBaseService;
import org.dcsa.ebl.model.ShipmentTransport;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface ShipmentTransportService extends ExtendedBaseService<ShipmentTransport, UUID> {
    Flux<ShipmentTransport> findByShipmentIDOrderBySequenceNumber(UUID shipmentID);
}
