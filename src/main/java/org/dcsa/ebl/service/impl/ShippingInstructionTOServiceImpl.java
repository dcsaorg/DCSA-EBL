package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.ebl.model.*;
import org.dcsa.ebl.model.base.AbstractCargoItem;
import org.dcsa.ebl.model.base.AbstractShippingInstruction;
import org.dcsa.ebl.model.transferobjects.CargoItemTO;
import org.dcsa.ebl.model.transferobjects.ShippingInstructionTO;
import org.dcsa.ebl.model.utils.MappingUtil;
import org.dcsa.ebl.service.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ShippingInstructionTOServiceImpl implements ShippingInstructionTOService {

    private final ShippingInstructionService shippingInstructionService;
    private final CargoItemService cargoItemService;
    private final CargoLineItemService cargoLineItemService;
    private final ReferenceService referenceService;


    @Override
    public Mono<ShippingInstructionTO> findById(UUID id) {
        ShippingInstructionTO shippingInstructionTO = new ShippingInstructionTO();
        return Flux.concat(
            shippingInstructionService.findById(id)
                    .doOnNext(shippingInstruction ->
                            MappingUtil.copyFields(
                                    shippingInstruction,
                                    shippingInstructionTO,
                                    AbstractShippingInstruction.class
                            )
                    ),
            cargoItemService.findAllByShippingInstructionID(id)
                .concatMap(cargoItem -> {
                    CargoItemTO cargoItemTO = MappingUtil.instanceFrom(cargoItem, CargoItemTO::new, AbstractCargoItem.class);

                    // cargoItemTO.equipmentReference is intentionally null

                    // TODO Performance: This suffers from N+1 syndrome (1 Query for the CargoItems and then N for the Cargo Lines)
                    return cargoLineItemService.findAllByCargoItemID(cargoItem.getId())
                            .collectList()
                            .doOnNext(cargoItemTO::setCargoLineItems)
                            .thenReturn(cargoItemTO);
                })
                .collectList()
                .doOnNext(shippingInstructionTO::setCargoItems),
           referenceService.findAllByShippingInstructionID(id)
                .collectList()
                .doOnNext(shippingInstructionTO::setReferences)
        )
                /* Consume all the items; we want the side-effect, not the return value */
                .then(Mono.just(shippingInstructionTO));
    }

    private Mono<Void> createCargoItems(UUID shippingInstructionID, Iterable<CargoItemTO> cargoItemTOs) {
        return Flux.fromIterable(cargoItemTOs)
                .flatMap(cargoItemTO -> {
                    CargoItem cargoItem = MappingUtil.instanceFrom(cargoItemTO, CargoItem::new, AbstractCargoItem.class);
                    cargoItem.setShippingInstructionID(shippingInstructionID);
                    return cargoItemService.create(cargoItem)
                            .flatMapMany(savedCargoItem -> {
                                UUID cargoItemId = savedCargoItem.getId();
                                List<CargoLineItem> cargoLineItems = cargoItemTO.getCargoLineItems();
                                cargoItemTO.setId(cargoItemId);
                                cargoLineItems.forEach(cli -> cli.setCargoItemID(cargoItemId));
                                return cargoLineItemService.createAll(cargoLineItems);
                            });
                })
                /* Consume all the items; we want the side-effect, not the return value */
                .then();
    }

    private Mono<Void> createReferences(UUID shippingInstructionID, Iterable<Reference> references) {
        return Flux.fromIterable(references)
                .flatMap(reference -> {
                    reference.setShippingInstructionID(shippingInstructionID);
                    return referenceService.create(reference)
                            .doOnNext(savedReference -> reference.setReferenceID(savedReference.getReferenceID()));
                })
                /* Consume all the items; we want the side-effect, not the return value */
                .then();
    }

    @Override
    public Mono<ShippingInstructionTO> create(ShippingInstructionTO shippingInstructionTO) {
        ShippingInstruction shippingInstruction = new ShippingInstruction();
        MappingUtil.copyFields(shippingInstructionTO, shippingInstruction, AbstractShippingInstruction.class);

        return shippingInstructionService.create(shippingInstructionTO.getShippingInstruction())
                .flatMapMany(savedShippingInstruction -> {
                    UUID shippingInstructionID = savedShippingInstruction.getId();
                    shippingInstructionTO.setId(savedShippingInstruction.getId());
                    return Flux.concat(
                            createCargoItems(shippingInstructionID, shippingInstructionTO.getCargoItems()),
                            createReferences(shippingInstructionID, shippingInstructionTO.getReferences())
                    );
                }).then(Mono.just(shippingInstructionTO));
    }

}
