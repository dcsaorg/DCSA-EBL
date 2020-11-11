package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.dcsa.ebl.model.CargoItem;
import org.dcsa.ebl.repository.CargoItemRepository;
import org.dcsa.ebl.service.CargoItemService;
import org.springframework.stereotype.Service;

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
}
