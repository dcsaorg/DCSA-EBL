package org.dcsa.ebl.service;

import org.dcsa.core.service.ExtendedBaseService;
import org.dcsa.ebl.model.Equipment;
import reactor.core.publisher.Mono;

public interface EquipmentService extends ExtendedBaseService<Equipment, String> {

    Mono<Equipment> createWithId(Equipment equipment);
}
