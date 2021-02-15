package org.dcsa.ebl.repository;

import org.dcsa.core.repository.ExtendedRepository;
import org.dcsa.ebl.model.ShipmentTransport;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface ShipmentTransportRepository extends ExtendedRepository<ShipmentTransport, UUID> {

    Flux<ShipmentTransport> findByShipmentIDOrderBySequenceNumber(UUID shipmentID);
}
