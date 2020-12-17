package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.dcsa.ebl.model.CargoLineItem;
import org.dcsa.ebl.repository.CargoLineItemRepository;
import org.dcsa.ebl.service.CargoLineItemService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class CargoLineItemServiceImpl extends ExtendedBaseServiceImpl<CargoLineItemRepository, CargoLineItem, UUID> implements CargoLineItemService {
    private final CargoLineItemRepository cargoLineItemRepository;


    @Override
    public CargoLineItemRepository getRepository() {
        return cargoLineItemRepository;
    }

    @Override
    public Class<CargoLineItem> getModelClass() {
        return CargoLineItem.class;
    }

    public Flux<CargoLineItem> findAllByCargoItemID(UUID shippingInstructionID) {
        return cargoLineItemRepository.findAllByCargoItemID(shippingInstructionID);
    }

    public Flux<CargoLineItem> createAll(Iterable<CargoLineItem> cargoLineItems) {
        return cargoLineItemRepository.saveAll(cargoLineItems);
    }
}