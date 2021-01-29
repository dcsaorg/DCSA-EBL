package org.dcsa.ebl.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.dcsa.core.exception.CreateException;
import org.dcsa.core.exception.UpdateException;
import org.dcsa.core.extendedrequest.ExtendedRequest;
import org.dcsa.ebl.ChangeSet;
import org.dcsa.ebl.model.*;
import org.dcsa.ebl.model.base.AbstractCargoItem;
import org.dcsa.ebl.model.base.AbstractDocumentParty;
import org.dcsa.ebl.model.base.AbstractShipmentLocation;
import org.dcsa.ebl.model.base.AbstractShippingInstruction;
import org.dcsa.ebl.model.enums.PartyFunction;
import org.dcsa.ebl.model.enums.ShipmentLocationType;
import org.dcsa.ebl.model.transferobjects.CargoItemTO;
import org.dcsa.ebl.model.transferobjects.DocumentPartyTO;
import org.dcsa.ebl.model.transferobjects.ShipmentEquipmentTO;
import org.dcsa.ebl.model.transferobjects.ShippingInstructionTO;
import org.dcsa.ebl.model.utils.MappingUtil;
import org.dcsa.ebl.repository.ActiveReeferSettingsRepository;
import org.dcsa.ebl.service.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.dcsa.ebl.ChangeSet.changeListDetector;
import static org.dcsa.ebl.ChangeSet.flatteningChangeDetector;
import static org.dcsa.ebl.Util.*;

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
    private final LocationService locationService;
    private final PartyService partyService;
    private final ReferenceService referenceService;
    private final SealService sealService;
    private final ShipmentEquipmentService shipmentEquipmentService;
    private final ShipmentLocationService shipmentLocationService;
    private final ShipmentService shipmentService;

    private final Validator validator;
    private final ObjectMapper objectMapper;

    private Mono<ShippingInstructionTO> extractShipmentRelatedFields(ShippingInstructionTO shippingInstructionTO,
                                                                     List<UUID> shipmentIDs,
                                                                     List<Tuple2<CargoItem, CargoItemTO>> cargoItemTuples
    ) {
        return Flux.concat(
                shipmentService.findAllById(shipmentIDs)
                    .collectMap(Shipment::getId, Shipment::getCarrierBookingReference)
                    .flatMapMany(shipmentId2BookingReference ->
                        Flux.fromIterable(cargoItemTuples)
                                .flatMap(tuple -> {
                                    CargoItem cargoItem = tuple.getT1();
                                    CargoItemTO cargoItemTO = tuple.getT2();
                                    String bookingReference = shipmentId2BookingReference.get(cargoItem.getShipmentID());
                                    if (bookingReference == null) {
                                        return Mono.error(new IllegalStateException("CargoItem " + cargoItem.getId()
                                                + " references Shipment " + cargoItem.getShipmentID()
                                                + " but we did not get its booking reference!?"));
                                    }
                                    cargoItemTO.setCarrierBookingReference(bookingReference);
                                    return Mono.empty();
                                })
                ),
                // TODO: Ideally, we would use a JOIN to pull the Location at the same time due to the
                // 1:1 relation between ShipmentLocation and Location
                shipmentLocationService.findAllByShipmentIDIn(shipmentIDs)
                    .flatMap(shipmentLocation -> Mono.zip(
                            Mono.just(shipmentLocation.getLocationID()),
                            Mono.just(MappingUtil.instanceFrom(
                                    shipmentLocation,
                                    ShipmentLocationTO::new,
                                    AbstractShipmentLocation.class
                            ))
                    // The same Location can (in theory) be used as different location types.
                    // Also, we have not de-duplicated yet.
                    )).collectMultimap(Tuple2::getT1, Tuple2::getT2)
                    .flatMapMany(locationId2ShipmentLocationTOs ->
                        setObjectOnAllMatchingInstances(
                                locationService.findAllById(locationId2ShipmentLocationTOs.keySet()),
                                locationId2ShipmentLocationTOs,
                                Location::getId,
                                ShipmentLocationTO::getLocation,
                                ShipmentLocationTO::setLocation,
                                "Location",
                                "ShipmentLocationTo"
                        )
                    // De-duplicate shipment locations - after converting them to TO objects, we might now have
                    // duplicates and the client cannot use those duplicates for anything.
                    ).distinct()
                    .collectList()
                    .doOnNext(shippingInstructionTO::setShipmentLocations),
                shipmentEquipmentService.findAllByShipmentIDIn(shipmentIDs)
                    .concatMap(shipmentEquipment -> {
                        ShipmentEquipmentTO shipmentEquipmentTO = new ShipmentEquipmentTO();

                        shipmentEquipmentTO.setId(shipmentEquipment.getId());
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
                                // ActiveReeferSettings is optional
                                activeReeferSettingsRepository.findById(shipmentEquipment.getId())
                                    .doOnNext(shipmentEquipmentTO::setActiveReeferSettings)
                        ).then(Mono.just(shipmentEquipmentTO));
                    }).collectList()
                    .doOnNext(shippingInstructionTO::setShipmentEquipments)
        ).then(Mono.just(shippingInstructionTO));
    }

    @Transactional
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
                            .then(Mono.zip(Mono.just(cargoItem), Mono.just(cargoItemTO)));
                })
                .collectList()
                .flatMapMany(tuples -> {
                    List<CargoItemTO> cargoItemTOs = tuples.stream().map(Tuple2::getT2).collect(Collectors.toList());
                    List<UUID> shipmentIds = tuples.stream().map(Tuple2::getT1).map(CargoItem::getShipmentID)
                            .distinct().collect(Collectors.toList());
                    return extractShipmentRelatedFields(shippingInstructionTO, shipmentIds, tuples)
                            .then(Mono.just(cargoItemTOs));
                })
                .doOnNext(shippingInstructionTO::setCargoItems)
                .count(),
           // TODO: Ideally we would use a JOIN to pull Party together with DocumentParty due to the 1:1 relation
           // but for now this will do.
           documentPartyService.findAllByShippingInstructionID(id)
                .flatMap(documentParty -> Mono.zip(
                               Mono.just(documentParty.getPartyID()),
                               Mono.just(MappingUtil.instanceFrom(
                                       documentParty,
                                       DocumentPartyTO::new,
                                       AbstractDocumentParty.class
                       ))
                )).collectMultimap(Tuple2::getT1, Tuple2::getT2)
                .flatMapMany(partyID2DocumentPartyTOs ->
                        setObjectOnAllMatchingInstances(
                                partyService.findAllById(partyID2DocumentPartyTOs.keySet()),
                                partyID2DocumentPartyTOs,
                                Party::getId,
                                DocumentPartyTO::getParty,
                                DocumentPartyTO::setParty,
                                "Party",
                                "DocumentPartyTO"
                        )
                )
                .collectList()
                .doOnNext(shippingInstructionTO::setDocumentParties),
           referenceService.findAllByShippingInstructionID(id)
                .collectList()
                .doOnNext(shippingInstructionTO::setReferences)
        )
                /* Consume all the items; we want the side-effect, not the return value */
                .then(Mono.just(shippingInstructionTO));
    }

    private Mono<Tuple2<Flux<CargoLineItem>, List<UUID>>> processCargoItems(UUID shippingInstructionID,
                                                  List<CargoItemTO> cargoItemTOs,
                                                  Map<String, UUID> equipmentReference2ID,
                                                  boolean creationFlow) {
        Map<UUID, String> usedEquipmentReferences = new HashMap<>();
        Function<String, RuntimeException> exceptionType = creationFlow ? CreateException::new : UpdateException::new;
        return Flux.fromIterable(cargoItemTOs)
                .map(CargoItemTO::getCarrierBookingReference)
                .buffer(SQL_LIST_BUFFER_SIZE)
                .concatMap(shipmentService::findByCarrierBookingReferenceIn)
                .collectMap(Shipment::getCarrierBookingReference, Shipment::getId)
                .flatMap(bookingReference2Shipment -> {
                    List<UUID> shipmentIDFlux = new ArrayList<>(bookingReference2Shipment.values());
                    Flux<CargoLineItem> cargoLineItemFlux = Flux.fromIterable(cargoItemTOs)
                            .flatMap(cargoItemTO -> {
                                CargoItem cargoItem = MappingUtil.instanceFrom(cargoItemTO, CargoItem::new, AbstractCargoItem.class);
                                String equipmentReference = cargoItemTO.getEquipmentReference();
                                List<CargoLineItem> cargoLineItems = cargoItemTO.getCargoLineItems();
                                String bookingReference = cargoItemTO.getCarrierBookingReference();
                                UUID shipmentEquipmentID;
                                UUID shipmentID;

                                if (creationFlow) {
                                    shipmentEquipmentID = equipmentReference2ID.get(equipmentReference);
                                    if (cargoItem.getId() != null) {
                                        return Mono.error(exceptionType.apply("The id of CargoItem is auto-generated: please omit it"));
                                    }
                                    if (shipmentEquipmentID == null) {
                                        return Mono.error(exceptionType.apply("Invalid equipment reference: " + equipmentReference));
                                    }
                                } else {
                                    UUID idFromEquipmentReference = equipmentReference2ID.get(equipmentReference);
                                    shipmentEquipmentID = cargoItem.getShipmentEquipmentID();
                                    if (equipmentReference != null && idFromEquipmentReference == null) {
                                        return Mono.error(exceptionType.apply("Invalid equipment reference: " + equipmentReference));
                                    }
                                    if (shipmentEquipmentID == null) {
                                        // Could be a newly crated equipment
                                        shipmentEquipmentID = idFromEquipmentReference;
                                    } else if (equipmentReference != null && !shipmentEquipmentID.equals(idFromEquipmentReference)) {
                                        return Mono.error(exceptionType.apply("Conflicting shipment equipment ID - explicit id is "
                                                + shipmentEquipmentID + " and ID via equipmentReference (" + equipmentReference
                                                + ") is: " + idFromEquipmentReference));
                                    }
                                    if (shipmentEquipmentID == null) {
                                        return Mono.error(exceptionType.apply("CargoItem must have either a shipmentEquipmentID"
                                                + " or a valid equipmentReference"));
                                    }
                                }

                                shipmentID = bookingReference2Shipment.get(bookingReference);
                                if (shipmentID == null) {
                                    return Mono.error(exceptionType.apply("Invalid booking reference: "
                                            + bookingReference));
                                }

                                usedEquipmentReferences.put(shipmentEquipmentID, equipmentReference);
                                cargoItem.setShipmentID(shipmentID);
                                cargoItem.setShippingInstructionID(shippingInstructionID);
                                cargoItem.setShipmentEquipmentID(shipmentEquipmentID);
                                // Update the TO variant to match
                                // Clear the EquipmentReference on exit because it is "input-only"
                                cargoItemTO.setEquipmentReference(null);
                                cargoItemTO.setShipmentEquipmentID(shipmentEquipmentID);
                                if (cargoLineItems == null || cargoLineItems.isEmpty()) {
                                    return Mono.error(exceptionType.apply("CargoItem with reference " + equipmentReference +
                                            ": Must have a field called cargoLineItems that is a non-empty list of cargo line items"
                                    ));
                                }
                                return Mono.just(cargoItem)
                                        .flatMap(cargoItemInner -> {
                                            if (cargoItem.getId() == null) {
                                                return cargoItemService.create(cargoItemInner);
                                            }
                                            return cargoItemService.update(cargoItemInner);
                                        })
                                        .doOnNext(savedCargoItem -> {
                                            UUID cargoItemId = savedCargoItem.getId();
                                            cargoItemTO.setId(cargoItemId);
                                            cargoLineItems.forEach(cli -> cli.setCargoItemID(cargoItemId));
                                        }).thenMany(Flux.fromIterable(cargoLineItems));
                            })
                            .doOnComplete(() -> {
                                for (UUID shipmentEquipmentID: equipmentReference2ID.values()) {
                                    if (!usedEquipmentReferences.containsKey(shipmentEquipmentID)) {
                                        String equipmentReference = usedEquipmentReferences.get(shipmentEquipmentID);
                                        if (equipmentReference == null) {
                                            equipmentReference = "N/A";
                                        }
                                        throw exceptionType.apply("Missing Cargo Items for equipment with ID "
                                                + shipmentEquipmentID + ", equipmentReference: " + equipmentReference);
                                    }
                                }
                            });

                    return Mono.zip(
                        Mono.just(cargoLineItemFlux),
                        Mono.just(shipmentIDFlux)
                    );
                });
    }

    private Mono<Void> mapReferences(UUID shippingInstructionID, Iterable<Reference> references, Function<Reference, Mono<Reference>> saveFunction) {
        return Flux.fromIterable(references)
                .flatMap(reference -> {
                    reference.setShippingInstructionID(shippingInstructionID);
                    return saveFunction.apply(reference)
                            .doOnNext(savedReference -> reference.setReferenceID(savedReference.getReferenceID()));
                })
                /* Consume all the items; we want the side-effect, not the return value */
                .then();
    }

    private Flux<Seal> processSeals(ShipmentEquipmentTO shipmentEquipmentTO, boolean mustBeCreate) {
        UUID shipmentEquipmentID = shipmentEquipmentTO.getId();
        List<Seal> seals = shipmentEquipmentTO.getSeals();
        if (seals == null || seals.isEmpty()) {
            return Flux.empty();
        }
        return Flux.fromIterable(seals)
                .flatMap(seal -> {
                    seal.setShipmentEquipmentID(shipmentEquipmentID);
                    if (mustBeCreate && seal.getId() != null) {
                        return Mono.error(new CreateException("New Seal instances must not have an ID.  Please remove ID " + seal.getId()
                                + " on  seal " + seal.getSealNumber() + " (type: " + seal.getSealType() + ", source: "
                                + seal.getSealSource() + ")"));
                    }
                    // TODO: This suffers from N+1 syndrome (1 SQL per seal)
                    return seal.getId() == null ? sealService.create(seal) : sealService.update(seal);
                });
    }

    private Mono<ShipmentEquipment> updateShipmentEquipmentFields(ShipmentEquipment shipmentEquipment,
                                                                  ShipmentEquipmentTO shipmentEquipmentTO) {
        if (shipmentEquipmentTO.getCargoGrossWeight() == null || shipmentEquipmentTO.getCargoGrossWeightUnit() == null) {
            return Mono.error(new CreateException("Error in ShipmentEquipment with equipmentReference "
                    + shipmentEquipment.getEquipmentReference() + ": Please include both cargoGrossWeight and cargoGrossWeightUnit"));
        }
        shipmentEquipment.setCargoGrossWeight(shipmentEquipmentTO.getCargoGrossWeight());
        shipmentEquipment.setCargoGrossWeightUnit(shipmentEquipmentTO.getCargoGrossWeightUnit());
        return Mono.just(shipmentEquipment);
    }

    private Mono<Tuple2<Map<String, UUID>, List<ShipmentEquipmentTO>>> updateEquipment(
            List<ShipmentEquipmentTO> shipmentEquipmentTOs,
            boolean nullReeferEnsuresAbsence
    ) {
        Map<String, UUID> referenceToDBId = new HashMap<>();
        return Flux.fromIterable(shipmentEquipmentTOs)
                .concatMap(shipmentEquipmentTO -> {
                    String equipmentReference = shipmentEquipmentTO.getEquipmentReference();

                    // TODO Performance: 1 Query for each ShipmentEquipment and then 1 for each ActiveReeferSettings
                    // This probably be reduced to one big "LEFT JOIN ... WHERE table.equipmentReference IN (LIST)".
                    return shipmentEquipmentService.findByEquipmentReference(equipmentReference)
                            .switchIfEmpty(Mono.error(new CreateException("Invalid equipment reference (create): " + equipmentReference)))
                            .<ShipmentEquipment>flatMap(shipmentEquipment -> {
                                referenceToDBId.put(equipmentReference, shipmentEquipment.getId());
                                shipmentEquipmentTO.setId(shipmentEquipment.getId());
                                return updateShipmentEquipmentFields(shipmentEquipment, shipmentEquipmentTO);
                            })
                            .flatMap(shipmentEquipmentService::update)
                            .flatMap(shipmentEquipment -> {
                                if (shipmentEquipmentTO.getActiveReeferSettings() == null) {
                                    if (nullReeferEnsuresAbsence) {
                                        return activeReeferSettingsRepository.findById(shipmentEquipment.getId())
                                                // We assume the Mono is empty and if not, then you are deleting an
                                                // active reefer from the equipment, which is not possible.
                                                .flatMap(activeReeferSettings ->
                                                        Mono.error(new UpdateException(
                                                                "Cannot remove ActiveReeferSettings on "
                                                                        + shipmentEquipmentTO.getEquipmentReference()
                                                                        + ": It is a built-in part of the equipment"
                                                        ))
                                                ).thenReturn(shipmentEquipment);
                                    }
                                    return activeReeferSettingsRepository.findById(shipmentEquipment.getId())
                                            .doOnNext(shipmentEquipmentTO::setActiveReeferSettings)
                                            .thenReturn(shipmentEquipment);
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
                                                                "Cannot add nor modify ActiveReeferSettings on "
                                                                        + shipmentEquipmentTO.getEquipmentReference()
                                                                        + ": The equipment does not have an active reefer."
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
                .then(Mono.zip(Mono.just(referenceToDBId), Mono.just(shipmentEquipmentTOs)));
    }

    private Mono<Void> mapParties(UUID shippingInstructionID, Iterable<DocumentPartyTO> documentPartyTOs,
                                  Function<DocumentParty, Mono<DocumentParty>> saveFunction) {
        return Flux.fromIterable(documentPartyTOs)
                .concatMap(documentPartyTO -> {
                    DocumentParty documentParty;
                    Party party = documentPartyTO.getParty();
                    UUID partyID = party.getId();
                    Mono<Party> partyMono;

                    documentParty = MappingUtil.instanceFrom(documentPartyTO, DocumentParty::new, AbstractDocumentParty.class);
                    documentParty.setShippingInstructionID(shippingInstructionID);

                    if (partyID != null) {
                        partyMono = partyService.findById(partyID)
                                .flatMap(existingParty -> {
                                    if (!existingParty.equals(party)) {
                                        return Mono.error(new UpdateException("Party with id " + partyID
                                                + " exists but has a different content. Remove the partyID field to"
                                                + " create a new instance or provide an update"));
                                    }
                                    return Mono.just(existingParty);
                                });
                    } else {
                        partyMono = partyService.create(party);
                    }
                    return partyMono
                            .doOnNext(resolvedParty -> {
                                documentParty.setPartyID(resolvedParty.getId());
                                documentPartyTO.setParty(resolvedParty);
                            }).flatMap(resolvedParty -> saveFunction.apply(documentParty));
                })
                .then();
    }

    private Mono<Void> processShipmentLocations(List<UUID> shipmentIDs, Iterable<ShipmentLocationTO> shipmentLocations) {
        return shipmentLocationService.updateAllRelatedFromTO(
                shipmentIDs,
                shipmentLocations,
                (shipmentLocation, shipmentLocationTO) -> {
                    shipmentLocation.setDisplayedName(shipmentLocationTO.getDisplayedName());
                    return Mono.just(shipmentLocation);
                });
    }

    @Transactional
    @Override
    public Mono<ShippingInstructionTO> create(ShippingInstructionTO shippingInstructionTO) {
        ShippingInstruction shippingInstruction = MappingUtil.instanceFrom(
                shippingInstructionTO,
                ShippingInstruction::new,
                AbstractShippingInstruction.class
        );
        return shippingInstructionService.create(shippingInstruction)
                .flatMapMany(savedShippingInstruction -> {
            UUID shippingInstructionID = savedShippingInstruction.getId();
            shippingInstructionTO.setId(savedShippingInstruction.getId());
            return updateEquipment(shippingInstructionTO.getShipmentEquipments(), false)
                    .flatMapMany(equipmentTuple ->
                        Flux.concat(
                                Flux.fromIterable(equipmentTuple.getT2())
                                    .concatMap(shipmentEquipmentTO -> processSeals(shipmentEquipmentTO, true)),
                                processCargoItems(
                                        shippingInstructionID,
                                        shippingInstructionTO.getCargoItems(),
                                        equipmentTuple.getT1(),
                                        true
                                ).flatMapMany(tuple -> {
                                    Flux<CargoLineItem> cargoLineItemFlux = tuple.getT1();
                                    List<UUID> shipmentIDs = tuple.getT2();
                                    return Flux.concat(
                                        cargoLineItemFlux
                                                .buffer(SQL_LIST_BUFFER_SIZE)
                                                .concatMap(cargoLineItemService::createAll),
                                            processShipmentLocations(shipmentIDs, shippingInstructionTO.getShipmentLocations())
                                    );
                                }),

                                mapReferences(
                                        shippingInstructionID,
                                        shippingInstructionTO.getReferences(),
                                        referenceService::create
                                ),
                                mapParties(
                                        shippingInstructionID,
                                        shippingInstructionTO.getDocumentParties(),
                                        documentPartyService::create
                                )
                        )
                    );
        }).then(Mono.just(shippingInstructionTO));
    }

    @Transactional
    @Override
    public Mono<ShippingInstructionTO> patchOriginal(UUID shippingInstructionId, JsonPatch patch) {
        return genericUpdate(shippingInstructionId, original -> {
            JsonNode target = objectMapper.convertValue(original, JsonNode.class);
            JsonNode jsonValue;
            try {
                jsonValue = patch.apply(target);
            } catch (JsonPatchException e) {
                throw new UpdateException(e.getMessage());
            }
            return objectMapper.convertValue(jsonValue, ShippingInstructionTO.class);
        });
    }

    @Transactional
    @Override
    public Mono<ShippingInstructionTO> replaceOriginal(UUID shippingInstructionId, ShippingInstructionTO update) {
        return genericUpdate(shippingInstructionId, original -> update);
    }

    @Transactional
    private Mono<ShippingInstructionTO> genericUpdate(UUID shippingInstructionId, Function<ShippingInstructionTO, ShippingInstructionTO> mutator) {
        return findById(shippingInstructionId)
                .flatMap(original -> {
                    ShippingInstructionTO update = mutator.apply(original);
                    Set<ConstraintViolation<ShippingInstructionTO>> violations = validator.validate(update);
                    if (!violations.isEmpty()) {
                        throw new ConstraintViolationException(violations);
                    }
                    if (!original.getId().equals(update.getId())) {
                        throw new UpdateException("Cannot change the ID of the ShippingInstruction");
                    }
                    ShippingInstruction updatedModel = MappingUtil.instanceFrom(
                            update,
                            ShippingInstruction::new,
                            AbstractShippingInstruction.class
                    );
                    ChangeSet<DocumentPartyTO> documentPartyTOChangeSet = ChangeSet.of(
                            List.copyOf(update.getDocumentParties()),
                            Collections.emptyList(),
                            List.copyOf(original.getDocumentParties())
                    );
                    ChangeSet<Reference> referenceChangeSet = changeListDetector(
                            original.getReferences(),
                            update.getReferences(),
                            Reference::getId,
                            fieldMustEqual("Reference", "shippingInstructionID",
                                    Reference::getShippingInstructionID, shippingInstructionId, true)
                    );
                    ChangeSet<ShipmentLocationTO> shipmentLocationChangeSet = changeListDetector(
                            original.getShipmentLocations(),
                            update.getShipmentLocations(),
                            // With our chosen ID, then the only "change" you can make is the display - anything else
                            // results in create/delete.
                            FakeShipmentLocationId::of,
                            acceptAny()
                    );
                    ChangeSet<CargoItemTO> cargoItemTOChangeSet = changeListDetector(
                            original.getCargoItems(),
                            update.getCargoItems(),
                            CargoItemTO::getId,
                            acceptAny()
                    );
                    ChangeSet<CargoLineItem> cargoLineItemChangeSet = cargoLineItemChangeDetector(
                            original.getCargoItems(),
                            update.getCargoItems()
                    );
                    ChangeSet<ShipmentEquipmentTO> shipmentEquipmentTOChangeSet = changeListDetector(
                            original.getShipmentEquipments(),
                            update.getShipmentEquipments(),
                            ShipmentEquipmentTO::getId,
                            acceptAny()
                    );
                    ChangeSet<Seal> sealChangeSet = sealChangeDetector(
                            original.getShipmentEquipments(),
                            update.getShipmentEquipments()
                    );
                    List<CargoItemTO> nonDeletedCargoItems = Stream.concat(
                            cargoItemTOChangeSet.newInstances.stream(),
                            cargoItemTOChangeSet.updatedInstances.stream()
                    ).collect(Collectors.toList());
                    Set<String> knownCargoLineItems = cargoLineItemChangeSet.updatedInstances.stream()
                            .map(CargoLineItem::getCargoLineItemID)
                            .collect(Collectors.toSet());
                    if (!shipmentLocationChangeSet.newInstances.isEmpty()) {
                        ShipmentLocationTO newInstance = shipmentLocationChangeSet.newInstances.get(0);
                        return Mono.error(new UpdateException("Cannot create ShipmentLocations"
                                + " (locationID, and locationType must not be changed): New instance is: "
                                + newInstance.getLocationID() + ", " + newInstance.getLocationType()
                                ));
                    }
                    if (!shipmentLocationChangeSet.orphanedInstances.isEmpty()) {
                        ShipmentLocationTO orphaned = shipmentLocationChangeSet.orphanedInstances.get(0);
                        return Mono.error(new UpdateException("Cannot delete ShipmentLocations"
                                + " (locationID, and locationType must not be changed): Deleted instance was: "
                                + orphaned.getLocationID() + ", " + orphaned.getLocationType()
                        ));
                    }
                    // TODO: Should it be possible to create/delete ShipmentEquipment instances (e.g. if you need to change  container)?
                    if (!shipmentEquipmentTOChangeSet.orphanedInstances.isEmpty()) {
                        ShipmentEquipmentTO deletedInstance = shipmentEquipmentTOChangeSet.orphanedInstances.get(0);
                        return Mono.error(new UnsupportedOperationException("Cannot delete ShipmentEquipment.  Deleted instance had ID: "
                                + deletedInstance.getId() + ", reference " + deletedInstance.getEquipmentReference()));
                    }
                    if (!shipmentEquipmentTOChangeSet.newInstances.isEmpty()) {
                        ShipmentEquipmentTO newInstance = shipmentEquipmentTOChangeSet.newInstances.get(0);
                        return Mono.error(new UnsupportedOperationException("Cannot create ShipmentEquipment. New instance had ID: "
                                + newInstance.getId() + ", reference " + newInstance.getEquipmentReference()));
                    }

                    Flux<?> deleteFirst = Flux.concat(
                            deleteAllFromChangeSet(sealChangeSet, sealService::delete),
                            deleteAllFromChangeSet(referenceChangeSet, referenceService::delete),
                            deleteObsoleteDocumentPartyInstances(documentPartyTOChangeSet.orphanedInstances),
                            // We delete obsolete cargo item and cargo line items first.  This avoids conflicts if a
                            // cargo line item is moved between two cargo items (as you can only use the ID once).
                            Flux.fromIterable(cargoLineItemChangeSet.orphanedInstances)
                                    .groupBy(CargoLineItem::getCargoItemID, CargoLineItem::getCargoLineItemID)
                                    .flatMap(uuidStringGroupedFlux ->
                                            uuidStringGroupedFlux.buffer(SQL_LIST_BUFFER_SIZE)
                                                    .concatMap(idList ->
                                                            cargoLineItemService.deleteByCargoItemIDAndCargoLineItemIDIn(
                                                                    uuidStringGroupedFlux.key(), idList))
                                    ).thenMany(Flux.fromStream(cargoItemTOChangeSet.orphanedInstances.stream().map(CargoItemTO::getId)))
                                    .buffer(SQL_LIST_BUFFER_SIZE)
                                    .concatMap(cargoItemService::deleteAllByIdIn)
                    );

                    Flux<Object> handleEquipmentAndCargoItems = updateEquipment(
                                shipmentEquipmentTOChangeSet.updatedInstances,
                                true
                            ).flatMapMany(equipmentTuple ->
                                Flux.fromIterable(equipmentTuple.getT2())
                                .flatMap(shipmentEquipmentTO -> processSeals(shipmentEquipmentTO, false))
                                .thenMany(
                                    processCargoItems(
                                            shippingInstructionId,
                                            nonDeletedCargoItems,
                                            equipmentTuple.getT1(),
                                            false
                                    )
                                ).flatMap(tuple -> {
                                    Flux<CargoLineItem> cargoLineItemFlux = tuple.getT1();
                                    List<UUID> shipmentIDs = tuple.getT2();

                                    return Flux.concat(
                                            cargoLineItemFlux
                                                .groupBy(cargoLineItem -> knownCargoLineItems.contains(cargoLineItem.getCargoLineItemID()))
                                                .flatMap(cargoLineItemGroup ->
                                                    cargoLineItemGroup.buffer(SQL_LIST_BUFFER_SIZE)
                                                            .concatMap(Objects.requireNonNull(cargoLineItemGroup.key())
                                                                    ? cargoLineItemService::updateAll
                                                                    : cargoLineItemService::createAll)
                                            ),
                                            processShipmentLocations(shipmentIDs, shipmentLocationChangeSet.updatedInstances)
                                    );
                                })
                    );

                    Flux<?> deferredUpdates = Flux.concat(
                            handleEquipmentAndCargoItems,
                            mapParties(
                                    shippingInstructionId,
                                    documentPartyTOChangeSet.newInstances,
                                    documentPartyService::create
                            ),
                            mapParties(
                                    shippingInstructionId,
                                    documentPartyTOChangeSet.updatedInstances,
                                    documentPartyService::update
                            ),
                            mapReferences(shippingInstructionId, referenceChangeSet.newInstances, referenceService::create),
                            mapReferences(shippingInstructionId, referenceChangeSet.updatedInstances, referenceService::update)
                    );

                    return deleteFirst
                            .thenMany(deferredUpdates)
                            // count + flatMap ensures a non-empty mono while trivially deferring the .update call
                            // The alternative .then(Mono.defer(() -> X)) is vastly harder to read.
                            .count()
                            .flatMap(ignored -> shippingInstructionService.update(updatedModel))
                            .thenReturn(update);
                });
    }

    private Mono<Void> deleteObsoleteDocumentPartyInstances(Iterable<DocumentPartyTO> documentPartyTOs) {
        return Flux.fromIterable(documentPartyTOs)
                .flatMap(documentPartyTO -> {
                    Party party = documentPartyTO.getParty();
                    UUID partyId = party.getId();
                    PartyFunction partyFunction = documentPartyTO.getPartyFunction();
                    if (partyId == null) {
                        return Mono.error(new AssertionError("Cannot delete a DocumentPartyTO without a" +
                                " partyID (on the Party member)"));
                    }
                    return Mono.zip(
                        Mono.just(partyId),
                        Mono.just(partyFunction),
                        documentPartyService.deleteByPartyIDAndPartyFunctionAndShipmentID(partyId, partyFunction, null)
                    );
                }).flatMap(tuple -> {
                    UUID partyID = tuple.getT1();
                    PartyFunction partyFunction = tuple.getT2();
                    int deletion = tuple.getT3();
                    switch (deletion) {
                        case 1:
                            // Deleted as expected; nothing more to do.
                            return Mono.empty();
                        case 0:
                            // No deletion, this is probably because there is a shipmentID as well.
                            // TODO: The implementation of this is based on an assumption of how this case should be handled.
                            // (The alternatively being deleting the DocumentParty even though it references a Shipment.
                            return documentPartyService.findByPartyIDAndPartyFunction(partyID, partyFunction)
                                    .doOnNext(documentParty -> documentParty.setShippingInstructionID(null))
                                    // Not the most efficient method, but will do for now.
                                    .flatMap(documentPartyService::update);
                        default:
                            return Mono.error(new AssertionError("Deleted " + deletion + " rows but expected it to be 0 or 1!?"));
                    }
                })
                .then();
    }

    private static <T> Mono<Void> deleteAllFromChangeSet(ChangeSet<T> tChangeSet, Function<T, Mono<Void>> singleItemDeleter) {
        return Flux.fromIterable(tChangeSet.orphanedInstances)
                .concatMap(singleItemDeleter)
                .then();
    }

    @Data
    private static class FakeShipmentLocationId {
        private final UUID locationID;
        private final ShipmentLocationType locationType;

        static FakeShipmentLocationId of(ShipmentLocationTO location) {
            return new FakeShipmentLocationId(location.getLocationID(), location.getLocationType());
        }
    }

    private static ChangeSet<Seal> sealChangeDetector(List<ShipmentEquipmentTO> itemsFromOriginal,
                                                           List<ShipmentEquipmentTO> itemsFromUpdate) {
        return flatteningChangeDetector(
                itemsFromOriginal,
                itemsFromUpdate,
                ShipmentEquipmentTO::getSeals,
                Seal::getId,
                "Seal"
        );
    }

    @Data(staticConstructor = "of")
    private static class FakeCargoLineItemID {
        final UUID cargoItemID;
        final String cargoLineItemID;

        public static FakeCargoLineItemID of(CargoLineItem cargoLineItem) {
            return of(cargoLineItem.getCargoItemID(), cargoLineItem.getCargoLineItemID());
        }
    }

    private static ChangeSet<CargoLineItem> cargoLineItemChangeDetector(List<CargoItemTO> itemsFromOriginal, List<CargoItemTO> itemsFromUpdate) {
        return flatteningChangeDetector(
                itemsFromOriginal,
                itemsFromUpdate,
                CargoItemTO::getCargoLineItems,
                FakeCargoLineItemID::of,
                "CargoLineItem"
        );
    }


    // This is a work around for missing 1:1 support via r2dbc.
    private <IID, TO, IO> Flux<TO> setObjectOnAllMatchingInstances(Flux<IO> ioObjectFlux,
                                                                   Map<IID, ? extends Iterable<TO>> id2TOMap,
                                                                   Function<IO, IID> idGetter,
                                                                   Function<TO, IO> to2ioGetter,
                                                                   BiConsumer<TO, IO> ioSetter,
                                                                   String innerObjectTypeName,
                                                                   String toObjectTypeName
    ) {
        return ioObjectFlux.flatMap(ioObject -> {
            Iterable<TO> list = id2TOMap.get(idGetter.apply(ioObject));
            if (list == null) {
                // We listed all known IDs, so this "should not happen" unless the code above
                // for generating the map changed.
                return Mono.error(new AssertionError("We pulled a " + innerObjectTypeName
                        + " by ID that we did not request!?"));
            }
            for (TO shipmentLocationTO : list) {
                if (to2ioGetter.apply(shipmentLocationTO) != null) {
                    return Mono.error(new AssertionError(toObjectTypeName + " already had a "
                            + innerObjectTypeName + "!?"));
                }
                ioSetter.accept(shipmentLocationTO, ioObject);
            }
            return Mono.empty();
        }).thenMany(Flux.fromIterable(id2TOMap.values()))
        .flatMap(Flux::fromIterable)
        .doOnNext(toObject -> {
            if (to2ioGetter.apply(toObject) == null) {
                throw new AssertionError("Found " +  toObjectTypeName + " without " + innerObjectTypeName + "!?");
            }
        });
    }

    public Flux<ShippingInstructionTO> findAllExtended(final ExtendedRequest<ShippingInstruction> extendedRequest) {
        return shippingInstructionService.findAllExtended(extendedRequest)
                .map(shippingInstruction -> MappingUtil.instanceFrom(shippingInstruction, ShippingInstructionTO::new, AbstractShippingInstruction.class));
    }
}
