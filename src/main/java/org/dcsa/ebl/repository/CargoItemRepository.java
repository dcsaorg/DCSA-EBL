package org.dcsa.ebl.repository;

import org.dcsa.core.repository.ExtendedRepository;
import org.dcsa.ebl.model.CargoItem;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface CargoItemRepository extends ExtendedRepository<CargoItem, UUID> {

    Flux<CargoItem> findAllByShippingInstructionID(String shippingInstructionID);
    Mono<Void> deleteAllByIdIn(List<UUID> cargoItemIDs);

}
