package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.exception.UpdateException;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.dcsa.ebl.model.CargoLineItem;
import org.dcsa.ebl.repository.CargoLineItemRepository;
import org.dcsa.ebl.service.CargoLineItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
        return cargoLineItemRepository.saveAll(
                Flux.fromIterable(cargoLineItems)
                    .concatMap(this::preCreateHook)
                    .concatMap(this::preSaveHook)
        );
    }

    @Override
    public Mono<CargoLineItem> findById(UUID id) {
        return Mono.error(new UnsupportedOperationException("findById not supported"));
    }

    protected Mono<CargoLineItem> preUpdateHook(CargoLineItem current, CargoLineItem update) {
        // FIXME: Revise this when we get compound Id support figured out
        // NB: We rely on this control check in the updateAll method
        if (! current.getCargoLineItemID().equals(update.getCargoLineItemID())) {
            return Mono.error(new UpdateException("update called with a non-matching item!"));
        }
        if (! current.getCargoItemID().equals(update.getCargoItemID())) {
            return Mono.error(new UpdateException("update called with a non-matching item!"));
        }
        update.setId(current.getId());
        return super.preUpdateHook(current, update);
    }

    @Transactional
    public Flux<CargoLineItem> updateAll(Iterable<CargoLineItem> cargoLineItems) {
        // TODO: We do one SELECT per unique cargoItemID and probably we can do better than that.
        return Flux.fromIterable(cargoLineItems)
                    .concatMap(cargoLineItem -> {
                        if (cargoLineItem.getCargoItemID() == null || cargoLineItem.getCargoLineItemID() == null) {
                            return Mono.error(new UpdateException("CargoLineItem must have non-null cargoItemID and cargoLineItemID for update"));
                        }
                        return Mono.just(cargoLineItem);
                    })
                    .groupBy(CargoLineItem::getCargoItemID)
                    .flatMap(keyedCargoLineItemFlux ->
                        keyedCargoLineItemFlux
                                .buffer(70)
                                .concatMap(bufferedCargoLineItems -> {
                                    UUID cargoItemID = keyedCargoLineItemFlux.key();
                                    List<String> cargoLineItemIDs;
                                    // Use Sort / OrderBy to ensure that bufferedCargoLineItems and the database results
                                    // are ordered in the same way (which ensures that .zip works as intended)
                                    bufferedCargoLineItems.sort(Comparator.comparing(CargoLineItem::getCargoLineItemID));
                                    cargoLineItemIDs = bufferedCargoLineItems.stream()
                                            .map(CargoLineItem::getCargoLineItemID)
                                            .collect(Collectors.toList());
                                    return Flux.zip(
                                            cargoLineItemRepository.findAllByCargoItemIDAndCargoLineItemIDInOrderByCargoLineItemID(
                                                    cargoItemID,
                                                    cargoLineItemIDs
                                            ),
                                            Flux.fromIterable(bufferedCargoLineItems)
                                    );
                                })
                    )
                    .concatMap(tuple -> {
                        // If the tuples get out of sync (which can happen because it is not an error in SQL
                        // for "x IN list" to have non-matching items in the ist), the preUpdateHook will
                        // take care of it.
                        CargoLineItem original = tuple.getT1();
                        CargoLineItem update = tuple.getT2();
                        return this.preUpdateHook(original, update);
                    })
                    .concatMap(this::preSaveHook)
                    .buffer(70)
                    .concatMap(cargoLineItemRepository::saveAll);
    }

    public Mono<Void> deleteByCargoItemIDAndCargoLineItemIDIn(UUID cargoItemID, List<String> cargoLineItemIDs) {
        return cargoLineItemRepository.deleteByCargoItemIDAndCargoLineItemIDIn(cargoItemID, cargoLineItemIDs);
    }
}
