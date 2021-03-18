package org.dcsa.ebl.service;

import org.dcsa.core.service.ExtendedBaseService;
import org.dcsa.ebl.model.combined.ExtendedShipmentTransport;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface ExtendedShipmentTransportService extends ExtendedBaseService<ExtendedShipmentTransport, UUID> {
    Flux<ExtendedShipmentTransport> findByShipmentIDOrderBySequenceNumber(UUID shipmentID);
}
