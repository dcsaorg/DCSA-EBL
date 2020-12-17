package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.dcsa.ebl.model.ShipmentEquipment;
import org.dcsa.ebl.repository.ShipmentEquipmentRepository;
import org.dcsa.ebl.service.ShipmentEquipmentService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ShipmentEquipmentServiceImpl extends ExtendedBaseServiceImpl<ShipmentEquipmentRepository, ShipmentEquipment, UUID> implements ShipmentEquipmentService {
    private final ShipmentEquipmentRepository shipmentEquipmentRepository;

    @Override
    public ShipmentEquipmentRepository getRepository() {
        return shipmentEquipmentRepository;
    }

    @Override
    public Class<ShipmentEquipment> getModelClass() {
        return ShipmentEquipment.class;
    }

    @Override
    public Flux<ShipmentEquipment> findAllByShipmentIDIn(List<UUID> shipmentIDs) {
        return shipmentEquipmentRepository.findAllByShipmentIDIn(shipmentIDs);
    }

    @Override
    public Mono<ShipmentEquipment> findByEquipmentReference(String equipmentReference) {
        return shipmentEquipmentRepository.findByEquipmentReference(equipmentReference);
    }
}
