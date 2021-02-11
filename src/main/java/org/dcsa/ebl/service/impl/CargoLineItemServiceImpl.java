package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.exception.UpdateException;
import org.dcsa.core.extendedrequest.ExtendedRequest;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.dcsa.ebl.model.CargoLineItem;
import org.dcsa.ebl.repository.CargoLineItemRepository;
import org.dcsa.ebl.service.CargoLineItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.dcsa.ebl.Util.SQL_LIST_BUFFER_SIZE;

@RequiredArgsConstructor
@Service
public class CargoLineItemServiceImpl implements CargoLineItemService {
    private final CargoLineItemRepository cargoLineItemRepository;

    public Flux<CargoLineItem> findAllByCargoItemID(UUID cargoItemID) {
        return cargoLineItemRepository.findAllByCargoItemID(cargoItemID);
    }

    @Override
    public Mono<CargoLineItem> create(CargoLineItem cargoLineItem) {
        return cargoLineItemRepository.insert(cargoLineItem);
    }

    public Flux<CargoLineItem> createAll(Iterable<CargoLineItem> cargoLineItems) {
        return Flux.fromIterable(cargoLineItems)
                .concatMap(cargoLineItemRepository::insert);
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

    public Mono<Void> deleteByCargoItemIDAndCargoLineItemIDIn(UUID cargoItemID, List<String> cargoLineItemIDs) {
        return cargoLineItemRepository.deleteByCargoItemIDAndCargoLineItemIDIn(cargoItemID, cargoLineItemIDs);
    }
}
