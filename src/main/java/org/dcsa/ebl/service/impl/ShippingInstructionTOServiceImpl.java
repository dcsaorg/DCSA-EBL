package org.dcsa.ebl.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.dcsa.core.exception.CreateException;
import org.dcsa.core.exception.UpdateException;
import org.dcsa.core.extendedrequest.ExtendedRequest;
import org.dcsa.ebl.model.*;
import org.dcsa.ebl.model.base.AbstractCargoItem;
import org.dcsa.ebl.model.base.AbstractDocumentParty;
import org.dcsa.ebl.model.base.AbstractShippingInstruction;
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

import javax.json.JsonPatch;
import javax.json.JsonStructure;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
public class ShippingInstructionTOServiceImpl implements ShippingInstructionTOService {

    /* For use with ".buffer(...).concatMap(service::createOrUpdateOrDeleteAll), etc. where the underlying
     * operation uses a variant "WHERE foo IN (LIST)".
     *
     * A higher number means fewer queries but after a certain size postgres performance will degrade.
     * Plus a higher number will also require more memory (java-side) as we build up a list of items.
     *
     * The number should be sufficient to bundle most trivial things into a single query without hitting
     * performance issues.
     */
    private static final int SQL_LIST_BUFFER_SIZE = 70;

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

    private final Validator validator;
    private final ObjectMapper objectMapper;


    private Mono<ShippingInstructionTO> extractShipmentRelatedFields(ShippingInstructionTO shippingInstructionTO, List<UUID> shipmentIDs) {
        if (shipmentIDs.size() != 1) {
            /* Assumption comes because we need to pull a booking reference from the shipment and that gives the 1:1 */
            return Mono.error(new UnsupportedOperationException("Expected Cargo items would lead to exactly" +
                    " one shipment, got " + shipmentIDs.size()));
        }
        return Flux.concat(
                shipmentService.findById(shipmentIDs.get(0))
                    .doOnNext(shipment ->
                            shippingInstructionTO.setCarrierBookingReference(shipment.getCarrierBookingReference())),
                shipmentLocationService.findAllByShipmentIDIn(shipmentIDs)
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
                    return extractShipmentRelatedFields(shippingInstructionTO, shipmentIds)
                            .then(Mono.just(cargoItemTOs));
                })
                .doOnNext(shippingInstructionTO::setCargoItems)
                .count(),
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

    private Flux<CargoLineItem> processCargoItems(UUID shippingInstructionID,
                                                  UUID shipmentID,
                                                  List<CargoItemTO> cargoItemTOs,
                                                  Map<String, UUID> equipmentReference2ID,
                                                  boolean createAll) {
        Set<String> usedEquipmentReferences = new HashSet<>();
        Function<String, RuntimeException> exceptionType = createAll ? CreateException::new : UpdateException::new;
        return Flux.fromIterable(cargoItemTOs)
                .flatMap(cargoItemTO -> {
                    CargoItem cargoItem = MappingUtil.instanceFrom(cargoItemTO, CargoItem::new, AbstractCargoItem.class);
                    String equipmentReference = cargoItemTO.getEquipmentReference();
                    List<CargoLineItem> cargoLineItems = cargoItemTO.getCargoLineItems();
                    UUID shipmentEquipmentID = equipmentReference2ID.get(equipmentReference);

                    if (createAll && cargoItem.getId() != null) {
                        return Mono.error(exceptionType.apply("The id of CargoItem is auto-generated: please omit it"));
                    }
                    if (shipmentEquipmentID == null) {
                        return Mono.error(exceptionType.apply("Invalid equipment reference: " + equipmentReference));
                    }
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
                    usedEquipmentReferences.add(equipmentReference);
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
                    for (String reference : equipmentReference2ID.keySet()) {
                        if (!usedEquipmentReferences.contains(reference)) {
                            throw exceptionType.apply("Missing Cargo Line Items for reference " + reference);
                        }
                    }
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
        if (shipmentEquipmentTO.getVerifiedGrossMass() == null) {
            return Mono.error(new CreateException("Error in ShipmentEquipment with equipmentReference "
                    + shipmentEquipment.getEquipmentReference() + ": Please include verifiedGrossMass"));
        }
        shipmentEquipment.setCargoGrossWeight(shipmentEquipmentTO.getCargoGrossWeight());
        shipmentEquipment.setCargoGrossWeightUnit(shipmentEquipmentTO.getCargoGrossWeightUnit());
        shipmentEquipment.setVerifiedGrossMass(shipmentEquipmentTO.getVerifiedGrossMass());
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
                    UUID partyID = documentPartyTO.getPartyID();

                    documentParty = MappingUtil.instanceFrom(documentPartyTO, DocumentParty::new, AbstractDocumentParty.class);
                    documentParty.setShippingInstructionID(shippingInstructionID);

                    if (partyID == null) {
                        return Mono.error(new CreateException("DocumentParty is missing required partyID field"));
                    }
                    if (party != null) {
                        return Mono.error(new CreateException("DocumentParty contains a Party object but we cannot"
                                + " create it via this call.  Please create the party separately and reference them"
                                + " via partyID"));
                    }
                    return partyService.findById(partyID)
                            .flatMap(resolvedParty -> saveFunction.apply(documentParty));
                })
                .then();
    }

    private Mono<Void> processShipmentLocations(UUID shipmentID, Iterable<ShipmentLocation> shipmentLocations) {
        return Flux.fromIterable(shipmentLocations)
                .flatMap(shipmentLocation -> {
                    UUID locationId = shipmentLocation.getLocationID();
                    ShipmentLocationType shipmentLocationType = shipmentLocation.getLocationType();
                    if (shipmentLocation.getShipmentID() != null && !shipmentID.equals(shipmentLocation.getShipmentID())) {
                        return Mono.error(new CreateException("Invalid shipmentID on shipmentLocation with locationID "
                                + locationId + " and locationType " + shipmentLocationType.name()
                                + ": You can omit the shipmentID field"));
                    }

                    // TODO: 1+N performance wise.  Ideally we would pull all of them in one go (should be doable
                    //  but not trivial with the current tooling provided by r2dbc).
                    return shipmentLocationService.findByShipmentIDAndLocationTypeAndLocationID(shipmentID,
                            shipmentLocationType,
                            locationId
                    ).switchIfEmpty(Mono.error(
                            new CreateException("Invalid shipmentLocation: Could not find any location with"
                                    + " locationID " + locationId + ", shipmentLocationType " + shipmentLocationType
                                    + " and shipmentID " + shipmentID + " related to this shipping instruction"
                            )
                    )).flatMap(originalShipmentLocation -> {
                        originalShipmentLocation.setDisplayedName(shipmentLocation.getDisplayedName());
                        return shipmentLocationService.update(originalShipmentLocation);
                    });
                })
                .then();
    }

    @Transactional
    @Override
    public Mono<ShippingInstructionTO> create(ShippingInstructionTO shippingInstructionTO) {
        ShippingInstruction shippingInstruction = MappingUtil.instanceFrom(
                shippingInstructionTO,
                ShippingInstruction::new,
                AbstractShippingInstruction.class
        );
        String bookingReference = shippingInstructionTO.getCarrierBookingReference();

        return Mono.zip(
                shipmentService.findByCarrierBookingReference(bookingReference)
                        .switchIfEmpty(Mono.error(new CreateException("Invalid booking reference: " + bookingReference))),
                shippingInstructionService.create(shippingInstruction)
        ).flatMapMany(tuple -> {
            Shipment shipment = tuple.getT1();
            ShippingInstruction savedShippingInstruction = tuple.getT2();
            UUID shippingInstructionID = savedShippingInstruction.getId();
            UUID shipmentID = shipment.getId();
            shippingInstructionTO.setId(savedShippingInstruction.getId());
            return updateEquipment(shippingInstructionTO.getShipmentEquipments(), false)
                    .flatMapMany(equipmentTuple ->
                        Flux.concat(
                                Flux.fromIterable(equipmentTuple.getT2())
                                    .concatMap(shipmentEquipmentTO -> processSeals(shipmentEquipmentTO, true)),
                                processCargoItems(
                                        shippingInstructionID,
                                        shipmentID,
                                        shippingInstructionTO.getCargoItems(),
                                        equipmentTuple.getT1(),
                                        true
                                )
                                        .buffer(SQL_LIST_BUFFER_SIZE)
                                        .concatMap(cargoLineItemService::createAll),
                                mapReferences(
                                        shippingInstructionID,
                                        shippingInstructionTO.getReferences(),
                                        referenceService::create
                                ),
                                mapParties(
                                        shippingInstructionID,
                                        shippingInstructionTO.getDocumentParties(),
                                        documentPartyService::create
                                ),
                                processShipmentLocations(shipmentID, shippingInstructionTO.getShipmentLocations())
                        )
                    );
        }).then(Mono.just(shippingInstructionTO));
    }

    @Transactional
    @Override
    public Mono<ShippingInstructionTO> patchOriginal(UUID shippingInstructionId, JsonPatch patch) {
        return genericUpdate(shippingInstructionId, original -> {
            JsonStructure target = objectMapper.convertValue(original, JsonStructure.class);
            JsonStructure jsonValue = patch.apply(target);
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
                    UUID shipmentID = original.getShipmentLocations().stream().map(ShipmentLocation::getShipmentID).findFirst().orElseThrow(
                            () -> new RuntimeException("No shipment location has a ShipmentID? (or there are no ShipmentLocations!?)")
                    );
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
                    ChangeSet<DocumentPartyTO> documentPartyTOChangeSet = changeListDetector(
                            original.getDocumentParties(),
                            update.getDocumentParties(),
                            AbstractDocumentParty::getPartyID,
                            acceptAny()
                    );
                    ChangeSet<Reference> referenceChangeSet = changeListDetector(
                            original.getReferences(),
                            update.getReferences(),
                            Reference::getId,
                            fieldMustEqual("Reference", "shippingInstructionID",
                                    Reference::getShippingInstructionID, shippingInstructionId, true)
                    );
                    ChangeSet<ShipmentLocation> shipmentLocationChangeSet = changeListDetector(
                            original.getShipmentLocations(),
                            update.getShipmentLocations(),
                            // With our chosen ID, then the only "change" you can make is the display - anything else
                            // results in create/delete.
                            FakeShipmentLocationId::of,
                            fieldMustEqual("ShipmentLocation", "shipmentID",
                                    ShipmentLocation::getShipmentID, shipmentID, true)
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
                        ShipmentLocation newInstance = shipmentLocationChangeSet.newInstances.get(0);
                        return Mono.error(new UpdateException("Cannot create ShipmentLocations"
                                + " (shipmentID, locationID, and locationType must not be changed): New instance is: "
                                + newInstance.getShipmentID() + ", " +  newInstance.getLocationID()
                                + ", " + newInstance.getLocationType()
                                ));
                    }
                    if (!shipmentLocationChangeSet.orphanedInstances.isEmpty()) {
                        ShipmentLocation orphaned = shipmentLocationChangeSet.orphanedInstances.get(0);
                        return Mono.error(new UpdateException("Cannot delete ShipmentLocations"
                                + " (shipmentID, locationID, and locationType must not be changed): Deleted instance was: "
                                + orphaned.getShipmentID() + ", " +  orphaned.getLocationID()
                                + ", " + orphaned.getLocationType()
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
                            deleteAllFromChangeSet(referenceChangeSet, referenceService::delete)
                    );

                    Flux<?> handleEquipmentAndCargoItems = updateEquipment(shipmentEquipmentTOChangeSet.updatedInstances, true)
                            .flatMapMany(equipmentTuple ->
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
                                        .flatMap(cargoItemService::deleteAllByIdIn)
                                        // With the deletion out of the way, we can move on to updating the cargo items
                                        // Use Flux.defer to avoid triggering any code in processCargoItems before we are ready
                                        .thenMany(Flux.defer(
                                                () -> processCargoItems(
                                                        shippingInstructionId,
                                                        shipmentID,
                                                        nonDeletedCargoItems,
                                                        equipmentTuple.getT1(),
                                                        false))
                                        ).groupBy(cargoLineItem -> knownCargoLineItems.contains(cargoLineItem.getCargoLineItemID()))
                                        .flatMap(cargoLineItemGroup ->
                                                cargoLineItemGroup.buffer(SQL_LIST_BUFFER_SIZE)
                                                    .concatMap(Objects.requireNonNull(cargoLineItemGroup.key())
                                                            ? cargoLineItemService::updateAll
                                                            : cargoLineItemService::createAll)
                                        ).thenMany(Flux.fromIterable(equipmentTuple.getT2()))
                                        .flatMap(shipmentEquipmentTO -> processSeals(shipmentEquipmentTO, false))
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
                            /*
                                TODO: Delete orphaned DocumentParty entries
                                 - Open question: How to handle document parties that have both shippingInstructionID
                                   AND shipmentID at the same time?  Should that be deleted or just have its
                                   shippingInstructionID cleared?
                                Flux.fromIterable(documentPartyTOChangeSet.orphanedInstances)
                                    .map(DocumentPartyTO::getPartyID)
                                    .concatMap(documentPartyService::deleteById)
                             */
                            Flux.fromIterable(documentPartyTOChangeSet.orphanedInstances)
                                    .count()
                                    .flatMap(count -> {
                                        if (count > 0) {
                                            return Mono.error(new UnsupportedOperationException("Not implemented yet"));
                                        }
                                        return Mono.empty();
                                    }),
                            mapReferences(shippingInstructionId, referenceChangeSet.newInstances, referenceService::create),
                            mapReferences(shippingInstructionId, referenceChangeSet.updatedInstances, referenceService::update),
                            processShipmentLocations(shipmentID, shipmentLocationChangeSet.updatedInstances)

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

    @Data(staticConstructor = "of")
    private static class ChangeSet<T> {
        final List<T> newInstances;
        final List<T> updatedInstances;
        final List<T> orphanedInstances;
    }

    private static <T> Mono<Void> deleteAllFromChangeSet(ChangeSet<T> tChangeSet, Function<T, Mono<Void>> singleItemDeleter) {
        return Flux.fromIterable(tChangeSet.orphanedInstances)
                .concatMap(singleItemDeleter)
                .then();
    }

    private static <T, I> Consumer<T> fieldMustEqual(String objectName, String fieldName, Function<T, I> getter,
                                                     I expectedValue, boolean allowNull) {
        return t -> {
            I value = getter.apply(t);
            if (value == null) {
                if (allowNull) {
                    return;
                }
            } else if (expectedValue.equals(value)) {
                return;
            }
            if (allowNull) {
                throw new UpdateException("The field " + fieldName + " on " + objectName
                        + " must either be omitted or set to " + expectedValue);
            }
            throw new UpdateException("The field " + fieldName + " on " + objectName + " must be " + expectedValue);
        };
    }

    private static <T> Consumer<T> acceptAny() {
        return (t) -> {};
    }

    @Data
    private static class FakeShipmentLocationId {
        private final UUID locationID;
        private final ShipmentLocationType locationType;

        static FakeShipmentLocationId of(ShipmentLocation location) {
            return new FakeShipmentLocationId(location.getLocationID(), location.getLocationType());
        }
    }

    private static <OM, IM, ID> ChangeSet<IM> flatteningChangeDetector(List<OM> itemsFromOriginal,
                                                                       List<OM> itemsFromUpdate,
                                                                       Function<OM, List<IM>> mapper,
                                                                       Function<IM, ID> idMapper,
                                                                       String name) {
        Function<OM, Stream<IM>> toIMStream = om -> {
            List<IM> imList = mapper.apply(om);
            if (imList == null || imList.isEmpty()) {
                return Stream.empty();
            }
            return imList.stream();
        };
        Map<ID, IM> knownIM = itemsFromOriginal.stream()
                .flatMap(toIMStream)
                .collect(Collectors.toMap(idMapper, Function.identity()));
        Set<ID> usedIds = new HashSet<>(knownIM.size());
        List<IM> updatedItems = new ArrayList<>(knownIM.size());
        List<IM> newItems = new ArrayList<>();
        itemsFromUpdate.stream()
                .flatMap(toIMStream)
                .forEach(im -> {
                    ID id = idMapper.apply(im);
                    if (knownIM.containsKey(id)) {
                        if (!usedIds.add(id)) {
                            throw new UpdateException(name + " ID " + id
                                    + " is used twice! IDs must be used at most once");
                        }
                        updatedItems.add(im);
                    } else {
                        newItems.add(im);
                    }
                });
        return ChangeSet.of(
                newItems,
                updatedItems,
                knownIM.entrySet().stream()
                        .filter(entry -> !usedIds.contains(entry.getKey()))
                        .map(Map.Entry::getValue)
                        .collect(Collectors.toList())
        );
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

    private static ChangeSet<CargoLineItem> cargoLineItemChangeDetector(List<CargoItemTO> itemsFromOriginal, List<CargoItemTO> itemsFromUpdate) {
        return flatteningChangeDetector(
                itemsFromOriginal,
                itemsFromUpdate,
                CargoItemTO::getCargoLineItems,
                CargoLineItem::getCargoLineItemID,
                "CargoLineItem"
        );
    }

    private static <T, I> ChangeSet<T> changeListDetector(List<T> listFromOriginal, List<T> listFromUpdate, Function<T, I> idMapper, Consumer<T> validator) {
        Map<I, T> knownIds = listFromOriginal.stream().collect(Collectors.toMap(idMapper, Function.identity()));
        Set<I> usedIds = new HashSet<>(listFromUpdate.size());
        List<T> newObjects = new ArrayList<>();
        List<T> updatedObjects = new ArrayList<>(listFromUpdate.size());

        for (T update : listFromUpdate) {
            I updateId = idMapper.apply(update);
            if (updateId != null) {
                if (!knownIds.containsKey(updateId)) {
                    throw new UpdateException("Invalid id: " + updateId
                            + ":  The id is not among the original list of ids (null the ID field if you want to"
                            + " create a new instance)");
                }
                usedIds.add(updateId);
            } else {
                newObjects.add(update);
            }
            validator.accept(update);
        }

        return ChangeSet.of(
                newObjects,
                updatedObjects,
                knownIds.entrySet().stream()
                        .filter(entry -> !usedIds.contains(entry.getKey()))
                        .map(Map.Entry::getValue)
                        .collect(Collectors.toList())
        );
    }

    public Flux<ShippingInstructionTO> findAllExtended(final ExtendedRequest<ShippingInstruction> extendedRequest) {
        return shippingInstructionService.findAllExtended(extendedRequest)
                .map(shippingInstruction -> MappingUtil.instanceFrom(shippingInstruction, ShippingInstructionTO::new, AbstractShippingInstruction.class));
    }
}
