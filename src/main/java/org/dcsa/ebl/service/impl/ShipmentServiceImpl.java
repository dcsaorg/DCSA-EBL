package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.events.model.Shipment;
import org.dcsa.core.events.repository.ShipmentRepository;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.dcsa.ebl.service.ShipmentService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ShipmentServiceImpl extends ExtendedBaseServiceImpl<ShipmentRepository, Shipment, UUID> implements ShipmentService {
    private final ShipmentRepository shipmentRepository;

    @Override
    public ShipmentRepository getRepository() {
        return shipmentRepository;
    }

    @Override
    public Flux<Shipment> findByCarrierBookingReferenceIn(List<String> carrierBookingReference) {
        return Flux.empty();
    }

    @Override
    public Mono<Shipment> findByCarrierBookingReference(String carrierBookingReference) {
        return shipmentRepository.findByCarrierBookingReference(carrierBookingReference);
    }

    @Override
    public Flux<Shipment> findAllById(Iterable<UUID> shipmentIDs) {
        return shipmentRepository.findAllById(shipmentIDs);
    }
}
