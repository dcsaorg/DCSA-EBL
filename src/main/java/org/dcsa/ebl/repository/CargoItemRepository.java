package org.dcsa.ebl.repository;

import org.dcsa.core.repository.ExtendedRepository;
import org.dcsa.ebl.model.CargoItem;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface CargoItemRepository extends ExtendedRepository<CargoItem, UUID> {

    Flux<CargoItem> findAllByShippingInstructionID(UUID shippingInstructionID);

}
