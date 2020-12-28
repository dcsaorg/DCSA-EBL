package org.dcsa.ebl.service;

import org.dcsa.core.service.ExtendedBaseService;
import org.dcsa.ebl.model.CargoLineItem;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface CargoLineItemService extends ExtendedBaseService<CargoLineItem, UUID> {

    Flux<CargoLineItem> findAllByCargoItemID(UUID cargoItemID);
    Flux<CargoLineItem> createAll(Iterable<CargoLineItem> cargoLineItems);
    Flux<CargoLineItem> updateAll(Iterable<CargoLineItem> cargoLineItems);
    Mono<Void> deleteByCargoItemIDAndCargoLineItemIDIn(UUID cargoItemID, List<String> cargoLineItemIDs);
}
