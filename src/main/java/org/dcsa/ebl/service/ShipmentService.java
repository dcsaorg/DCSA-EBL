package org.dcsa.ebl.service;

import org.dcsa.core.service.ExtendedBaseService;
import org.dcsa.ebl.model.Shipment;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

public interface ShipmentService extends ExtendedBaseService<Shipment, UUID> {
    Flux<Shipment> findByCarrierBookingReferenceIn(List<String> carrierBookingReference);
    Flux<Shipment> findByCarrierBookingReference(String carrierBookingReference);
    Flux<Shipment> findAllById(Iterable<UUID> shipmentIDs);
}
