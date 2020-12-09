package org.dcsa.ebl.repository;

import org.dcsa.core.repository.ExtendedRepository;
import org.dcsa.ebl.model.CargoLineItem;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface CargoLineItemRepository extends ExtendedRepository<CargoLineItem, UUID> {

    Flux<CargoLineItem> findAllByCargoItemID(UUID cargoItemID);

}
