package org.dcsa.ebl.service;

import org.dcsa.core.service.ExtendedBaseService;
import org.dcsa.ebl.model.Shipment;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ShipmentService extends ExtendedBaseService<Shipment, UUID> {
    Mono<Shipment> findByCarrierBookingReference(String carrierBookingReference);
}
