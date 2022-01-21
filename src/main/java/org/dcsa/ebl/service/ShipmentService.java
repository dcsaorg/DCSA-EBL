package org.dcsa.ebl.service;

import org.dcsa.core.events.model.Shipment;
import org.dcsa.core.service.ExtendedBaseService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface ShipmentService extends ExtendedBaseService<Shipment, UUID> {
    Flux<Shipment> findByCarrierBookingReferenceIn(List<String> carrierBookingReference);
    Mono<Shipment> findByCarrierBookingReference(String carrierBookingReference);
    Flux<Shipment> findAllById(Iterable<UUID> shipmentIDs);
}
