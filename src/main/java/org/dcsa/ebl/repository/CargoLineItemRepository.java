package org.dcsa.ebl.repository;

import org.dcsa.core.repository.ExtendedRepository;
import org.dcsa.ebl.model.CargoLineItem;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface CargoLineItemRepository extends ExtendedRepository<CargoLineItem, UUID>, InsertAddonRepository<CargoLineItem> {

    Flux<CargoLineItem> findAllByCargoItemID(UUID cargoItemID);
    Mono<Void> deleteByCargoItemID(UUID cargoItemID);
    Mono<Void> deleteByCargoItemIDAndCargoLineItemIDIn(UUID cargoItemID, List<String> cargoLineItemIDs);
}
