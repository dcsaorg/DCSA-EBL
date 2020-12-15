package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.dcsa.ebl.model.Equipment;
import org.dcsa.ebl.repository.EquipmentRepository;
import org.dcsa.ebl.service.EquipmentService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class EquipmentServiceImpl extends ExtendedBaseServiceImpl<EquipmentRepository, Equipment, String> implements EquipmentService {
    private final EquipmentRepository equipmentRepository;


    @Override
    public EquipmentRepository getRepository() {
        return equipmentRepository;
    }

    @Override
    public Class<Equipment> getModelClass() {
        return Equipment.class;
    }
}
