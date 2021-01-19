package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.dcsa.ebl.model.Shipment;
import org.dcsa.ebl.repository.ShipmentRepository;
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
    public Class<Shipment> getModelClass() {
        return Shipment.class;
    }

    @Override
    public Flux<Shipment> findByCarrierBookingReferenceIn(List<String> carrierBookingReference) {
        return shipmentRepository.findByCarrierBookingReferenceIn(carrierBookingReference);
    }

    @Override
    public Flux<Shipment> findAllById(Iterable<UUID> shipmentIDs) {
        return shipmentRepository.findAllById(shipmentIDs);
    }
}
