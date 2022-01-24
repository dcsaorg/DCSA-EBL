package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.events.model.Equipment;
import org.dcsa.core.events.repository.EquipmentRepository;
import org.dcsa.core.exception.CreateException;
import org.dcsa.core.exception.UpdateException;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.dcsa.ebl.model.base.AbstractEquipment;
import org.dcsa.ebl.model.transferobjects.EquipmentTO;
import org.dcsa.ebl.model.transferobjects.ShipmentEquipmentTO;
import org.dcsa.ebl.model.utils.MappingUtil;
import org.dcsa.ebl.service.EquipmentService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
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
    public Mono<Void> ensureEquipmentExistAndMatchesRequest(Iterable<ShipmentEquipmentTO> shipmentEquipmentTOs) {
        return Mono.empty();
    }
}
