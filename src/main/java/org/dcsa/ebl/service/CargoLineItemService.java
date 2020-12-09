package org.dcsa.ebl.service;

import org.dcsa.core.service.ExtendedBaseService;
import org.dcsa.ebl.model.CargoLineItem;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface CargoLineItemService extends ExtendedBaseService<CargoLineItem, UUID> {

    Flux<CargoLineItem> findAllByCargoItemID(UUID cargoItemID);


    Flux<CargoLineItem> createAll(Iterable<CargoLineItem> cargoLineItems);
}
