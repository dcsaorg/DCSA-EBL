package org.dcsa.ebl.service;

import org.dcsa.core.service.ExtendedBaseService;
import org.dcsa.ebl.model.CargoItem;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface CargoItemService extends ExtendedBaseService<CargoItem, UUID> {

    Flux<CargoItem> findAllByShippingInstructionID(UUID shippingInstructionID);
    Mono<Void> deleteAllByIdIn(List<UUID> cargoItemIDs);
}
