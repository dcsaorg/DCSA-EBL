package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.exception.CreateException;
import org.dcsa.ebl.model.*;
import org.dcsa.ebl.model.base.AbstractCargoItem;
import org.dcsa.ebl.model.base.AbstractDocumentParty;
import org.dcsa.ebl.model.base.AbstractShippingInstruction;
import org.dcsa.ebl.model.transferobjects.CargoItemTO;
import org.dcsa.ebl.model.transferobjects.DocumentPartyTO;
import org.dcsa.ebl.model.transferobjects.ShipmentEquipmentTO;
import org.dcsa.ebl.model.transferobjects.ShippingInstructionTO;
import org.dcsa.ebl.model.utils.MappingUtil;
import org.dcsa.ebl.service.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ShippingInstructionTOServiceImpl implements ShippingInstructionTOService {

    private final ShippingInstructionService shippingInstructionService;

    private final ActiveReeferSettingsService activeReeferSettingsService;
    private final CargoItemService cargoItemService;
    private final CargoLineItemService cargoLineItemService;
    private final DocumentPartyService documentPartyService;
    private final EquipmentService equipmentService;
    private final PartyService partyService;
    private final ReferenceService referenceService;
    private final SealService sealService;
    private final ShipmentEquipmentService shipmentEquipmentService;
    private final ShipmentLocationService shipmentLocationService;
    private final ShipmentService shipmentService;


    private Mono<ShippingInstructionTO> extractShipmentRelatedFields(ShippingInstructionTO shippingInstructionTO, List<UUID> shipmentIDs) {
        return Flux.concat(
                shipmentLocationService.findAllByShipmentIDIn(shipmentIDs)
                    .collectList()
                    .doOnNext(shippingInstructionTO::setShipmentLocations),
                shipmentEquipmentService.findAllByShipmentIDIn(shipmentIDs)
                    .concatMap(shipmentEquipment -> {
                        ShipmentEquipmentTO shipmentEquipmentTO = new ShipmentEquipmentTO();

                        shipmentEquipmentTO.setShipmentEquipmentID(shipmentEquipment.getId());
                        shipmentEquipmentTO.setEquipmentReference(shipmentEquipment.getEquipmentReference());
                        shipmentEquipmentTO.setVerifiedGrossMass(shipmentEquipment.getVerifiedGrossMass());
                        shipmentEquipmentTO.setCargoGrossWeight(shipmentEquipment.getCargoGrossWeight());
                        shipmentEquipmentTO.setCargoGrossWeightUnit(shipmentEquipment.getCargoGrossWeightUnit().name());

                        // TODO Performance: This suffers from N+1 syndrome (1 Query for the ShipmentEquipment
                        //  and then N for the ActiveReeferSettings + N for the Equipment + N for the Seals)
                        //
                        // ActiveReeferSettings + Equipment should be doable with a trivial 1:1 JOIN between
                        // ShipmentEquipment, Equipment, and ActiveReeferSettings.  Seals are a bit more
                        // problematic as it will force a lot of data to be repeated for each seal (plus r2dbc
                        // does not have a good solution for 1:N relations at the moment)
                        //
                        // Anyway, we start here and can improve it later.
                        return Flux.concat(
                                sealService.findAllByShipmentEquipmentID(shipmentEquipment.getId())
                                    .collectList()
                                    .doOnNext(shipmentEquipmentTO::setSeals),
                                activeReeferSettingsService.findByShipmentEquipmentID(shipmentEquipment.getId())
                                    .doOnNext(shipmentEquipmentTO::setActiveReeferSettings),
                                equipmentService.findById(shipmentEquipment.getEquipmentReference())
                                    .doOnNext(equipment -> {
                                        shipmentEquipmentTO.setIsoEquipmentCode(equipment.getIsoEquipmentCode());
                                        shipmentEquipmentTO.setContainerTareWeight(equipment.getTareWeight());
                                        shipmentEquipmentTO.setContainerTareWeightUnit(equipment.getWeightUnit().name());
                                        shipmentEquipmentTO.setWeightUnit(equipment.getWeightUnit().name());
                                    })
                        ).then(Mono.just(shipmentEquipmentTO));
                    }).collectList()
                    .doOnNext(shippingInstructionTO::setShipmentEquipments)
        ).then(Mono.just(shippingInstructionTO));
    }

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
                .doOnNext(shippingInstructionTO::setCargoItems)
                .flatMapMany(cargoItemTOs -> {
                    List<UUID> shipmentIds = cargoItemTOs.stream().map(CargoItemTO::getShipmentID)
                            .distinct().collect(Collectors.toList());

                    return extractShipmentRelatedFields(shippingInstructionTO, shipmentIds);

                }),
           documentPartyService.findAllByShippingInstructionID(id)
                .map(documentParty ->
                        MappingUtil.instanceFrom(documentParty, DocumentPartyTO::new, AbstractDocumentParty.class))
                .collectList()
                .doOnNext(shippingInstructionTO::setDocumentParties),
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
                    shipmentEquipment.setVerifiedGrossMass(shipmentEquipmentTO.getVerifiedGrossMass());
                    shipmentEquipment.setCargoGrossWeight(shipmentEquipmentTO.getCargoGrossWeight());
                    shipmentEquipment.setCargoGrossWeightUnit(shipmentEquipmentTO.getCargoGrossWeightUnit());

                    equipment.setEquipmentReference(shipmentEquipmentTO.getEquipmentReference());
                    equipment.setTareWeight(shipmentEquipmentTO.getContainerTareWeight());
                    equipment.setWeightUnit(shipmentEquipmentTO.getWeightUnit());
                    equipment.setIsoEquipmentCode(shipmentEquipmentTO.getIsoEquipmentCode());

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

    private Mono<Void> mapParties(UUID shippingInstructionID, UUID shipmentID, Iterable<DocumentPartyTO> documentPartyTOs) {
        return Flux.fromIterable(documentPartyTOs)
                .concatMap(documentPartyTO -> {
                    DocumentParty documentParty;
                    Party party = documentPartyTO.getParty();
                    Mono<Party> partyMono;

                    documentPartyTO.setShippingInstructionID(shippingInstructionID);

                    documentParty = MappingUtil.instanceFrom(documentPartyTO, DocumentParty::new, AbstractDocumentParty.class);
                    documentParty.setShipmentID(shipmentID);

                    if (party == null) {
                        UUID partyID = documentPartyTO.getPartyID();
                        if (partyID == null) {
                            return Mono.error(new CreateException("DocumentParty did not have a partyID nor a party field; please include exactly one of these fields"));
                        }
                        partyMono = partyService.findById(partyID);
                    } else {
                        if (documentPartyTO.getPartyID() != null) {
                            return Mono.error(new CreateException("DocumentParty had both a partyID and a party field; please include exactly one of these fields"));
                        }
                        partyMono = partyService.create(party);
                    }
                    return partyMono.flatMap(resolvedParty -> {
                        documentParty.setPartyID(resolvedParty.getId());
                        documentPartyTO.setParty(null);
                        return documentPartyService.create(documentParty);
                    });
                })
                .then();
    }

    private Mono<Void> mapShipmentLocations(UUID shipmentID, Iterable<ShipmentLocation> shipmentLocations) {
        return Flux.fromIterable(shipmentLocations)
                .flatMap(shipmentLocation -> {
                    shipmentLocation.setShipmentID(shipmentID);
                    return shipmentLocationService.create(shipmentLocation);
                })
                .then();
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
                                createReferences(shippingInstructionID, shippingInstructionTO.getReferences()),
                                mapParties(
                                        shippingInstructionID,
                                        shipment.getId(),
                                        shippingInstructionTO.getDocumentParties()
                                ),
                                mapShipmentLocations(shipment.getId(), shippingInstructionTO.getShipmentLocations())
                        )
                    );
        }).then(Mono.just(shippingInstructionTO));
    }

}
