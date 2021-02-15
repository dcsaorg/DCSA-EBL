package org.dcsa.ebl.repository;

import org.dcsa.core.repository.ExtendedRepository;
import org.dcsa.ebl.model.Shipment;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

public interface ShipmentRepository extends ExtendedRepository<Shipment, UUID> {

    Flux<Shipment> findByCarrierBookingReferenceIn(List<String> carrierBookingReferences);
    Flux<Shipment> findByCarrierBookingReference(String carrierBookingReferences);
}
