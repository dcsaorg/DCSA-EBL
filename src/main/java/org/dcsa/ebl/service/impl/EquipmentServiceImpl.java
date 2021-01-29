package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.dcsa.ebl.model.Equipment;
import org.dcsa.ebl.repository.EquipmentRepository;
import org.dcsa.ebl.service.EquipmentService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

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

    @Override
    public Mono<Equipment> createWithId(Equipment equipment) {
        // .create does not work because it assumes it should use an UPDATE as the object
        // has an ID.
        return Mono.error(new UnsupportedOperationException("Not implemented yet"));
    }
}
