package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.dcsa.ebl.model.CargoLineItem;
import org.dcsa.ebl.repository.CargoLineItemRepository;
import org.dcsa.ebl.service.CargoLineItemService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class CargoLineItemServiceImpl extends ExtendedBaseServiceImpl<CargoLineItemRepository, CargoLineItem, UUID> implements CargoLineItemService {
    private final CargoLineItemRepository cargoLineItemRepository;

    @Override
    public CargoLineItemRepository getRepository() {
        return cargoLineItemRepository;
    }

    public Flux<CargoLineItem> findAllByCargoItemID(UUID cargoItemID) {
        return cargoLineItemRepository.findAllByCargoItemID(cargoItemID);
    }

    @Override
    public Mono<CargoLineItem> create(CargoLineItem cargoLineItem) {
        return cargoLineItemRepository.save(cargoLineItem);
    }

    public Flux<CargoLineItem> createAll(Iterable<CargoLineItem> cargoLineItems) {
        return Flux.fromIterable(cargoLineItems)
                .concatMap(cargoLineItemRepository::save);
    }

    @Override
    public Mono<CargoLineItem> findById(UUID id) {
        return Mono.error(new UnsupportedOperationException("findById not supported"));
    }


    @Override
    public Mono<Void> deleteById(UUID id) {
        return Mono.error(new UnsupportedOperationException("deleteById not supported"));
    }

    @Override
    public Mono<Void> delete(CargoLineItem cargoLineItem) {
        return cargoLineItemRepository.deleteByCargoItemIDAndCargoLineItemIDIn(
                cargoLineItem.getCargoItemID(),
                Collections.singletonList(cargoLineItem.getCargoLineItemID())
        );
    }

    @Override
    public UUID getIdOfEntity(CargoLineItem entity) {
        return entity.getId();
    }

    @Override
    public Flux<CargoLineItem> findAll() {
        return cargoLineItemRepository.findAll();
    }

    @Override
    public Mono<CargoLineItem> save(CargoLineItem cargoLineItem) {
        return Mono.error(new UnsupportedOperationException("save not supported"));
    }

    @Override
    public Mono<CargoLineItem> update(CargoLineItem cargoLineItem) {
        return Mono.error(new UnsupportedOperationException("update not supported"));
    }

    @Override
    public Mono<Void> deleteByCargoItemID(UUID cargoItemID) {
        return cargoLineItemRepository.deleteByCargoItemID(cargoItemID);
    }
}
