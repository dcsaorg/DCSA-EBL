package org.dcsa.ebl.repository;

import org.dcsa.ebl.model.CargoLineItem;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface CargoLineItemRepository extends R2dbcRepository<CargoLineItem, UUID>, InsertAddonRepository<CargoLineItem> {

    Flux<CargoLineItem> findAllByCargoItemID(UUID cargoItemID);
    Mono<Void> deleteByCargoItemIDAndCargoLineItemIDIn(UUID cargoItemID, List<String> cargoLineItemIDs);
}
