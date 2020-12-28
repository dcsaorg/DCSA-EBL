package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.exception.UpdateException;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.dcsa.ebl.model.CargoItem;
import org.dcsa.ebl.repository.CargoItemRepository;
import org.dcsa.ebl.service.CargoItemService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class CargoItemServiceImpl extends ExtendedBaseServiceImpl<CargoItemRepository, CargoItem, UUID> implements CargoItemService {
    private final CargoItemRepository cargoItemRepository;


    @Override
    public CargoItemRepository getRepository() {
        return cargoItemRepository;
    }

    @Override
    public Class<CargoItem> getModelClass() {
        return CargoItem.class;
    }

    public Flux<CargoItem> findAllByShippingInstructionID(UUID shippingInstructionID) {
        return cargoItemRepository.findAllByShippingInstructionID(shippingInstructionID);
    }

    public Mono<Void> deleteAllByIdIn(List<UUID> cargoItemIDs) {
        return cargoItemRepository.deleteAllByIdIn(cargoItemIDs);
    }

    @Override
    protected Mono<CargoItem> preSaveHook(CargoItem cargoItem) {
        if (cargoItem.getShipmentID() == null) {
            return Mono.error(new UpdateException("CargoItem must have a non-null shipmentID"));
        }
        return super.preSaveHook(cargoItem);
    }
}
