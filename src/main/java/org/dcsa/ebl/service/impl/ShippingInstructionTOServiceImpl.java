package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.ebl.model.*;
import org.dcsa.ebl.model.base.AbstractCargoItem;
import org.dcsa.ebl.model.base.AbstractShippingInstruction;
import org.dcsa.ebl.model.transferobjects.CargoItemTO;
import org.dcsa.ebl.model.transferobjects.ShipmentEquipmentTO;
import org.dcsa.ebl.model.transferobjects.ShippingInstructionTO;
import org.dcsa.ebl.model.utils.MappingUtil;
import org.dcsa.ebl.service.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.constraints.Size;
import java.util.*;

@RequiredArgsConstructor
@Service
public class ShippingInstructionTOServiceImpl implements ShippingInstructionTOService {

    private final ShippingInstructionService shippingInstructionService;

    private final ActiveReeferSettingsService activeReeferSettingsService;
    private final CargoItemService cargoItemService;
    private final CargoLineItemService cargoLineItemService;
    private final EquipmentService equipmentService;
    private final ReferenceService referenceService;
    private final SealService sealService;
    private final ShipmentEquipmentService shipmentEquipmentService;
    private final ShipmentService shipmentService;



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

    private Mono<Void> createCargoItems(UUID shippingInstructionID,
                                        Iterable<CargoItemTO> cargoItemTOs,
                                        Map<String, UUID> equipmentReference2ID) {
        return Flux.fromIterable(cargoItemTOs)
                .flatMap(cargoItemTO -> {
                    CargoItem cargoItem = MappingUtil.instanceFrom(cargoItemTO, CargoItem::new, AbstractCargoItem.class);
                    UUID shipmentEquipmentID = Objects.requireNonNull(equipmentReference2ID.get(cargoItemTO.getEquipmentReference()));
                    cargoItem.setShippingInstructionID(shippingInstructionID);
                    cargoItem.setShipmentEquipmentID(shipmentEquipmentID);
                    // Clear the EquipmentReference on exit because it is "input-only"
                    cargoItemTO.setEquipmentReference(null);
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

    private Mono<Void> createSeals(UUID shipmentEquipmentID, Iterable<Seal> seals) {
        return Flux.fromIterable(seals)
                .flatMap(seal -> {
                    seal.setShipmentEquipmentID(shipmentEquipmentID);
                    return sealService.create(seal);
                }).then();
    }

    private Mono<Map<String, UUID>> createEquipment(Shipment shipment, Iterable<ShipmentEquipmentTO> shipmentEquipmentTOs) {
        Map<String, UUID> referenceToDBId = new HashMap<>();
        return Flux.fromIterable(shipmentEquipmentTOs)
                .map(shipmentEquipmentTO -> {
                    Equipment equipment = new Equipment();
                    ShipmentEquipment shipmentEquipment = new ShipmentEquipment();
                    shipmentEquipment.setShipmentID(shipment.getId());
                    shipmentEquipment.setEquipmentReference(shipmentEquipmentTO.getEquipmentReference());
                    // FIXME: Type discrepancy between IM and IF
                    // shipmentEquipment.setVerifiedGrossMass(shipmentEquipmentTO.getVerifiedGrossMass());

                    equipment.setEquipmentReference(shipmentEquipmentTO.getEquipmentReference());
                    equipment.setWeightUnit(shipmentEquipmentTO.getWeightUnit());

                    /* Order is important due to FK constraints between Equipment and ShipmentEquipment */
                    return equipmentService.create(equipment)
                            .flatMap(ignored -> shipmentEquipmentService.create(shipmentEquipment))
                            .flatMap(savedShipmentEquipment -> {
                                UUID shipmentEquipmentID = savedShipmentEquipment.getId();
                                ActiveReeferSettings activeReeferSettings = shipmentEquipmentTO.getActiveReeferSettings();
                                shipmentEquipmentTO.setShipmentEquipmentID(shipmentEquipmentID);
                                referenceToDBId.put(savedShipmentEquipment.getEquipmentReference(), shipmentEquipmentID);
                                activeReeferSettings.setShipmentEquipmentID(shipmentEquipmentID);
                                return activeReeferSettingsService.create(activeReeferSettings)
                                        .then(createSeals(shipmentEquipmentID, shipmentEquipmentTO.getSeals()));
                            });
                })
                .then(Mono.just(referenceToDBId));
    }

    @Override
    public Mono<ShippingInstructionTO> create(ShippingInstructionTO shippingInstructionTO) {
        ShippingInstruction shippingInstruction = new ShippingInstruction();
        MappingUtil.copyFields(shippingInstructionTO, shippingInstruction, AbstractShippingInstruction.class);

        return Mono.zip(
                shipmentService.findByCarrierBookingReference(shippingInstructionTO.getCarrierBookingReference()),
                shippingInstructionService.create(shippingInstructionTO.getShippingInstruction())
        ).flatMapMany(tuple -> {
            Shipment shipment = tuple.getT1();
            ShippingInstruction savedShippingInstruction = tuple.getT2();
            UUID shippingInstructionID = savedShippingInstruction.getId();
            shippingInstructionTO.setId(savedShippingInstruction.getId());
            return createEquipment(shipment, shippingInstructionTO.getShipmentEquipments())
                    .flatMapMany(equipmentReference2ID ->
                        Flux.concat(
                                createCargoItems(
                                        shippingInstructionID,
                                        shippingInstructionTO.getCargoItems(),
                                        equipmentReference2ID
                                ),
                                createReferences(shippingInstructionID, shippingInstructionTO.getReferences())
                        )
                    );
        }).then(Mono.just(shippingInstructionTO));
    }

}
