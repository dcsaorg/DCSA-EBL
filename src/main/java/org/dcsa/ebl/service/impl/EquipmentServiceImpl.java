package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.dcsa.ebl.model.Equipment;
import org.dcsa.ebl.model.Shipment;
import org.dcsa.ebl.repository.EquipmentRepository;
import org.dcsa.ebl.repository.ShipmentRepository;
import org.dcsa.ebl.service.EquipmentService;
import org.dcsa.ebl.service.ShipmentService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class EquipmentServiceImpl extends ExtendedBaseServiceImpl<EquipmentRepository, Equipment, String> implements EquipmentService {
    private final org.dcsa.ebl.repository.EquipmentRepository EquipmentRepository;


    @Override
    public EquipmentRepository getRepository() {
        return EquipmentRepository;
    }

    @Override
    public Class<Equipment> getModelClass() {
        return Equipment.class;
    }
}
