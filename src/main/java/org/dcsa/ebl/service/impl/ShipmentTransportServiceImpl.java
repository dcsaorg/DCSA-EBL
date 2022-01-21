package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.events.model.ShipmentTransport;
import org.dcsa.core.events.repository.ShipmentTransportRepository;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.dcsa.ebl.service.ShipmentTransportService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ShipmentTransportServiceImpl extends ExtendedBaseServiceImpl<ShipmentTransportRepository, ShipmentTransport, UUID> implements ShipmentTransportService {
    private final ShipmentTransportRepository shipmentTransportRepository;

    @Override
    public ShipmentTransportRepository getRepository() {
        return shipmentTransportRepository;
    }

    @Override
    public Flux<ShipmentTransport> findByShipmentIDOrderBySequenceNumber(UUID shipmentID) {
        return Flux.empty();
    }
}
