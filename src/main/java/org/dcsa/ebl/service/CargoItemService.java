package org.dcsa.ebl.service;

import org.dcsa.core.service.ExtendedBaseService;
import org.dcsa.ebl.model.CargoItem;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CargoItemService extends ExtendedBaseService<CargoItem, UUID> {

    Flux<CargoItem> findAllByShippingInstructionID(String shippingInstructionID);
    Mono<Void> deleteAllCargoItemsOnShippingInstruction(String shippingInstructionID);
}
