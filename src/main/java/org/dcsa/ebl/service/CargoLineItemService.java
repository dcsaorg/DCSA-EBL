package org.dcsa.ebl.service;

import org.dcsa.core.service.BaseService;
import org.dcsa.ebl.model.CargoLineItem;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CargoLineItemService extends BaseService<CargoLineItem, UUID> {

    Flux<CargoLineItem> findAllByCargoItemID(UUID cargoItemID);

    @Transactional
    Flux<CargoLineItem> createAll(Iterable<CargoLineItem> cargoLineItems);

    @Transactional
    Mono<Void> deleteByCargoItemID(UUID cargoItemID);
}
