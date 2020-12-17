package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.exception.CreateException;
import org.dcsa.core.extendedrequest.ExtendedRequest;
import org.dcsa.ebl.model.*;
import org.dcsa.ebl.model.base.AbstractCargoItem;
import org.dcsa.ebl.model.base.AbstractDocumentParty;
import org.dcsa.ebl.model.base.AbstractShippingInstruction;
import org.dcsa.ebl.model.transferobjects.CargoItemTO;
import org.dcsa.ebl.model.transferobjects.DocumentPartyTO;
import org.dcsa.ebl.model.transferobjects.ShipmentEquipmentTO;
import org.dcsa.ebl.model.transferobjects.ShippingInstructionTO;
import org.dcsa.ebl.model.utils.MappingUtil;
import org.dcsa.ebl.repository.ActiveReeferSettingsRepository;
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

    /* We need the repository because the service gives an error if the object does not exist */
    private final ActiveReeferSettingsRepository activeReeferSettingsRepository;
    private final ActiveReeferSettingsService activeReeferSettingsService;
    private final CargoItemService cargoItemService;
    private final CargoLineItemService cargoLineItemService;
    private final DocumentPartyService documentPartyService;
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
                        // ActiveReeferSettings + Equipment should be doable with a trivial 1:1 (LEFT) JOIN between
                        // ShipmentEquipment, Equipment, and ActiveReeferSettings.  Seals are a bit more
                        // problematic as it will force a lot of data to be repeated for each seal (plus r2dbc
                        // does not have a good solution for 1:N relations at the moment)
                        //
                        // Anyway, we start here and can improve it later.
                        return Flux.concat(
                                sealService.findAllByShipmentEquipmentID(shipmentEquipment.getId())
                                    .collectList()
                                    .doOnNext(shipmentEquipmentTO::setSeals),
                                // ActiveRefeerSettings is optional
                                activeReeferSettingsRepository.findById(shipmentEquipment.getId())
                                    .doOnNext(shipmentEquipmentTO::setActiveReeferSettings)
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

    private Mono<Map<String, UUID>> createEquipment(Iterable<ShipmentEquipmentTO> shipmentEquipmentTOs) {
        Map<String, UUID> referenceToDBId = new HashMap<>();
        return Flux.fromIterable(shipmentEquipmentTOs)
                .map(shipmentEquipmentTO -> {
                    String equipmentReference = shipmentEquipmentTO.getEquipmentReference();

                    // TODO Performance: 1 Query for each ShipmentEquipment and then 1 for each ActiveReeferSettings
                    // This probably be reduced to one big "LEFT JOIN ... WHERE shipmentEquipmentId IN (LIST)".
                    return shipmentEquipmentService.findByEquipmentReference(equipmentReference)
                            .doOnNext(shipmentEquipment -> {
                                shipmentEquipment.setEquipmentReference(shipmentEquipmentTO.getEquipmentReference());
                                shipmentEquipment.setVerifiedGrossMass(shipmentEquipmentTO.getVerifiedGrossMass());
                                shipmentEquipment.setCargoGrossWeight(shipmentEquipmentTO.getCargoGrossWeight());
                                shipmentEquipment.setCargoGrossWeightUnit(shipmentEquipmentTO.getCargoGrossWeightUnit());
                                referenceToDBId.put(equipmentReference, shipmentEquipment.getId());
                            }).flatMap(shipmentEquipmentService::save)
                            .flatMap(shipmentEquipment ->
                                    createSeals(shipmentEquipment.getId(), shipmentEquipmentTO.getSeals())
                                            .thenReturn(shipmentEquipment)
                            ).flatMap(shipmentEquipment -> {
                                if (shipmentEquipmentTO.getActiveReeferSettings() == null) {
                                    // Short cut: If no changes to the ActiveReeferSettings is requested, then we save
                                    // the look up.
                                    return Mono.empty();
                                }
                                /*
                                 * ActiveReeferSettings can be absent; abort if it is absent AND there is an attempt to
                                 * change it.
                                 */
                                return activeReeferSettingsRepository.findById(shipmentEquipment.getId())
                                        .switchIfEmpty(
                                                // We get here if there was no ActiveReeferSettings related to the
                                                // Shipment Equipment
                                                Mono.error(new CreateException(
                                                                "Cannot modify ActiveReeferSettings on "
                                                                        + shipmentEquipmentTO.getEquipmentReference()
                                                                        + ": It does not have an active reefer."
                                                        ))
                                        ).flatMap(current -> {
                                            ActiveReeferSettings update = shipmentEquipmentTO.getActiveReeferSettings();
                                            if (update.getShipmentEquipmentID() != null) {
                                                return Mono.error(new CreateException(
                                                        "Cannot modify ActiveReeferSettings on "
                                                                + shipmentEquipmentTO.getEquipmentReference()
                                                                + ": Please omit shipmentEquipmentID (it is implicit)"));
                                            }
                                            return activeReeferSettingsService.update(update);
                                        });
                            });
                })
                .then(Mono.just(referenceToDBId));
    }

    private Mono<Void> mapParties(UUID shippingInstructionID, UUID shipmentID, Iterable<DocumentPartyTO> documentPartyTOs) {
        return Flux.fromIterable(documentPartyTOs)
                .concatMap(documentPartyTO -> {
                    DocumentParty documentParty;
                    Party party = documentPartyTO.getParty();
                    UUID partyID = documentPartyTO.getPartyID();

                    documentPartyTO.setShippingInstructionID(shippingInstructionID);
                    documentParty = MappingUtil.instanceFrom(documentPartyTO, DocumentParty::new, AbstractDocumentParty.class);
                    documentParty.setShipmentID(shipmentID);

                    if (partyID == null) {
                        return Mono.error(new CreateException("DocumentParty is missing required partyID field"));
                    }
                    if (party != null) {
                        return Mono.error(new CreateException("DocumentParty contains a Party object but we cannot"
                                + " create it via this call.  Please create the party separately and reference them"
                                + " via partyID"));
                    }
                    return partyService.findById(partyID)
                            .flatMap(resolvedParty -> documentPartyService.create(documentParty));
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
        ShippingInstruction shippingInstruction = MappingUtil.instanceFrom(
                shippingInstructionTO,
                ShippingInstruction::new,
                AbstractShippingInstruction.class
        );

        return Mono.zip(
                shipmentService.findByCarrierBookingReference(shippingInstructionTO.getCarrierBookingReference()),
                shippingInstructionService.create(shippingInstruction)
        ).flatMapMany(tuple -> {
            Shipment shipment = tuple.getT1();
            ShippingInstruction savedShippingInstruction = tuple.getT2();
            UUID shippingInstructionID = savedShippingInstruction.getId();
            shippingInstructionTO.setId(savedShippingInstruction.getId());
            return createEquipment(shippingInstructionTO.getShipmentEquipments())
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

    public Flux<ShippingInstructionTO> findAllExtended(final ExtendedRequest<ShippingInstruction> extendedRequest) {
        return shippingInstructionService.findAllExtended(extendedRequest)
                .map(shippingInstruction -> MappingUtil.instanceFrom(shippingInstruction, ShippingInstructionTO::new, AbstractShippingInstruction.class));
    }
}
