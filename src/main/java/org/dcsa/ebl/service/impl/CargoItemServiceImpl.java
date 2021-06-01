package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.exception.UpdateException;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.dcsa.ebl.Util;
import org.dcsa.ebl.model.CargoItem;
import org.dcsa.ebl.repository.CargoItemRepository;
import org.dcsa.ebl.service.CargoItemService;
import org.dcsa.ebl.service.CargoLineItemService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class CargoItemServiceImpl extends ExtendedBaseServiceImpl<CargoItemRepository, CargoItem, UUID> implements CargoItemService {
    private final CargoItemRepository cargoItemRepository;
    private final CargoLineItemService cargoLineItemService;


    @Override
    public CargoItemRepository getRepository() {
        return cargoItemRepository;
    }

    @Override
    public Flux<CargoItem> findAllByShippingInstructionID(String shippingInstructionID) {
        return cargoItemRepository.findAllByShippingInstructionID(shippingInstructionID);
    }

    @Override
    public Mono<Void> deleteAllCargoItemsOnShippingInstruction(String shippingInstructionID) {
        return findAllByShippingInstructionID(shippingInstructionID)
                .flatMap(cargoItem ->
                        cargoLineItemService.deleteByCargoItemID(cargoItem.getId())
                        .thenReturn(cargoItem)
                ).buffer(Util.SQL_LIST_BUFFER_SIZE)
                .concatMap(cargoItemRepository::deleteAll)
                .then();
    }

    @Override
    protected Mono<CargoItem> preSaveHook(CargoItem cargoItem) {
        if (cargoItem.getShipmentID() == null) {
            return Mono.error(new UpdateException("CargoItem must have a non-null shipmentID"));
        }
        return super.preSaveHook(cargoItem);
    }
}
