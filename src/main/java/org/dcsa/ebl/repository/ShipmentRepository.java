package org.dcsa.ebl.repository;

import org.dcsa.core.repository.ExtendedRepository;
import org.dcsa.ebl.model.Shipment;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ShipmentRepository extends ExtendedRepository<Shipment, UUID> {

    Mono<Shipment> findByCarrierBookingReference(String carrierBookingReference);
}
