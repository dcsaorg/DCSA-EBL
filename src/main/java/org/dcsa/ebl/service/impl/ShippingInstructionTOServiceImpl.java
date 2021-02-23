package org.dcsa.ebl.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import lombok.RequiredArgsConstructor;
import org.dcsa.core.exception.CreateException;
import org.dcsa.core.exception.UpdateException;
import org.dcsa.core.extendedrequest.ExtendedRequest;
import org.dcsa.ebl.ChangeSet;
import org.dcsa.ebl.Util;
import org.dcsa.ebl.model.*;
import org.dcsa.ebl.model.base.*;
import org.dcsa.ebl.model.transferobjects.*;
import org.dcsa.ebl.model.utils.MappingUtil;
import org.dcsa.ebl.model.utils.ShippingInstructionUpdateInfo;
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
    private final EquipmentService equipmentService;
    private final LocationService locationService;
    private final PartyService partyService;
    private final ReferenceService referenceService;
    private final SealService sealService;
    private final ShipmentEquipmentService shipmentEquipmentService;
    private final ShipmentService shipmentService;

    private final Validator validator;
    private final ObjectMapper objectMapper;

    private Mono<Void> processFreightPayableAt(ShippingInstructionTO shippingInstructionTO, ShippingInstruction shippingInstruction) {
        return Mono.justOrEmpty(shippingInstructionTO.getFreightPayableAt())
                .flatMap(locationService::resolveLocation)
                .doOnNext(shippingInstructionTO::setFreightPayableAt)
                .map(Location::getId)
                .doOnNext(shippingInstruction::setFreightPayableAt)
                .then();
    }

    private Mono<ShippingInstructionTO> extractShipmentRelatedFields(ShippingInstructionTO shippingInstructionTO,
                                                                     List<UUID> shipmentIDs,
                                                                     List<Tuple2<CargoItem, CargoItemTO>> cargoItemTuples
    ) {
        Map<UUID, List<CargoItemTO>> shipmentEquipmentID2CargoItems = cargoItemTuples.stream().collect(
                Collectors.groupingBy(
                        tuple -> tuple.getT1().getShipmentEquipmentID(),
                        Collectors.mapping(Tuple2::getT2, Collectors.toList())
                )
        );
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
                shipmentEquipmentService.findAllByShipmentIDIn(shipmentIDs)
                    .concatMap(shipmentEquipment -> {
                        ShipmentEquipmentTO shipmentEquipmentTO = new ShipmentEquipmentTO();
                        List<CargoItemTO> cargoItemTOs = shipmentEquipmentID2CargoItems.get(shipmentEquipment.getId());

                        shipmentEquipmentTO.setCargoGrossWeight(shipmentEquipment.getCargoGrossWeight());
                        shipmentEquipmentTO.setCargoGrossWeightUnit(shipmentEquipment.getCargoGrossWeightUnit().name());

                        if (cargoItemTOs == null || cargoItemTOs.isEmpty()) {
                            return Mono.error(new IllegalStateException("No CargoItem referenced Equipment with "
                                    + shipmentEquipment.getEquipmentReference() + "!?"));
                        }
                        for (CargoItemTO cargoItemTO : cargoItemTOs) {
                            cargoItemTO.setEquipmentReference(shipmentEquipment.getEquipmentReference());
                        }

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
                                equipmentService.findById(shipmentEquipment.getEquipmentReference())
                                        .map(equipment -> MappingUtil.instanceFrom(equipment, EquipmentTO::new, AbstractEquipment.class))
                                        .doOnNext(shipmentEquipmentTO::setEquipment),
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
            locationService.findPaymentLocationByShippingInstructionID(id)
                .doOnNext(shippingInstructionTO::setFreightPayableAt),
            cargoItemService.findAllByShippingInstructionID(id)
                .concatMap(cargoItem -> {
                    CargoItemTO cargoItemTO = MappingUtil.instanceFrom(cargoItem, CargoItemTO::new, AbstractCargoItem.class);

                    // cargoItemTO.equipmentReference is intentionally null

                    // TODO Performance: This suffers from N+1 syndrome (1 Query for the CargoItems and then N for the Cargo Lines)
                    return cargoLineItemService.findAllByCargoItemID(cargoItem.getId())
                            .map(cargoLineItem -> MappingUtil.instanceFrom(cargoLineItem, CargoLineItemTO::new, AbstractCargoLineItem.class))
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
                .doOnNext(cargoItemTOs -> {
                    shippingInstructionTO.setCargoItems(cargoItemTOs);
                    shippingInstructionTO.hoistCarrierBookingReferenceIfPossible();
                })
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

    private Mono<ShippingInstructionUpdateInfo> loadShipmentIDs(ShippingInstructionUpdateInfo instructionUpdateInfo) {
        List<CargoItemTO> cargoItemTOs = instructionUpdateInfo.getShippingInstructionTO().getCargoItems();
        HashMap<String, String> equipmentReference2CarrierBookingReference = new HashMap<>(cargoItemTOs.size());
        for (CargoItemTO cargoItemTO : cargoItemTOs) {
            String carrierBookingReference = cargoItemTO.getCarrierBookingReference();
            String equipmentReference = cargoItemTO.getEquipmentReference();
            String existing = equipmentReference2CarrierBookingReference.putIfAbsent(equipmentReference, carrierBookingReference);
            if (existing != null && !existing.equals(carrierBookingReference)) {
                return Mono.error(new UpdateException("EquipmentReference " + equipmentReference
                        + " used for two distinct booking references at the same time (" + existing + ", "
                        + carrierBookingReference + ")"));
            }
        }
        instructionUpdateInfo.setEquipmentReference2CarrierBookingReference(
                Collections.unmodifiableMap(equipmentReference2CarrierBookingReference)
        );
        return Flux.fromIterable(cargoItemTOs)
                .map(CargoItemTO::getCarrierBookingReference)
                .buffer(SQL_LIST_BUFFER_SIZE)
                .concatMap(shipmentService::findByCarrierBookingReferenceIn)
                .collectMap(Shipment::getCarrierBookingReference, Shipment::getId)
                .doOnNext(instructionUpdateInfo::setCarrierBookingReference2ShipmentID)
                .thenReturn(instructionUpdateInfo);
    }

    private Mono<Void> processCargoItems(
            ShippingInstructionUpdateInfo shippingInstructionUpdateInfo,
            List<CargoItemTO> cargoItemTOs,
            boolean creationFlow
    ) {
        Map<UUID, String> usedEquipmentReferences = new HashMap<>();
        Function<String, RuntimeException> exceptionType = creationFlow ? CreateException::new : UpdateException::new;
        UUID shippingInstructionID = shippingInstructionUpdateInfo.getShippingInstructionID();
        Map<String, UUID> equipmentReference2ID = shippingInstructionUpdateInfo.getEquipmentReference2ShipmentEquipmentID();
        Map<String, UUID> bookingReference2Shipment = shippingInstructionUpdateInfo.getCarrierBookingReference2ShipmentID();
        return Flux.fromIterable(cargoItemTOs)
                .flatMap(cargoItemTO -> {
                    CargoItem cargoItem = MappingUtil.instanceFrom(cargoItemTO, CargoItem::new, AbstractCargoItem.class);
                    String equipmentReference = cargoItemTO.getEquipmentReference();
                    List<CargoLineItemTO> cargoLineItemTOs = cargoItemTO.getCargoLineItems();
                    String bookingReference = cargoItemTO.getCarrierBookingReference();
                    UUID shipmentEquipmentID = equipmentReference2ID.get(equipmentReference);
                    UUID shipmentID = bookingReference2Shipment.get(bookingReference);

                    if (shipmentEquipmentID == null) {
                        return Mono.error(exceptionType.apply("Invalid equipment reference: " + equipmentReference));
                    }
                    if (shipmentID == null) {
                        return Mono.error(exceptionType.apply("Invalid booking reference: "
                                + bookingReference));
                    }
                    if (creationFlow && cargoItem.getId() != null) {
                        return Mono.error(exceptionType.apply("The id of CargoItem is auto-generated: please omit it"));
                    }

                    usedEquipmentReferences.put(shipmentEquipmentID, equipmentReference);
                    cargoItem.setShipmentID(shipmentID);
                    cargoItem.setShippingInstructionID(shippingInstructionID);
                    cargoItem.setShipmentEquipmentID(shipmentEquipmentID);

                    if (cargoLineItemTOs == null || cargoLineItemTOs.isEmpty()) {
                        return Mono.error(exceptionType.apply("CargoItem with equipment reference " + equipmentReference +
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
                            .flatMapMany(savedCargoItem -> {
                                UUID cargoItemID = savedCargoItem.getId();
                                cargoItemTO.setId(cargoItemID);
                                return Flux.fromIterable(cargoLineItemTOs)
                                        .map(cargoLineItemTO -> {
                                            CargoLineItem cargoLineItem = MappingUtil.instanceFrom(
                                                    cargoLineItemTO,
                                                    CargoLineItem::new,
                                                    AbstractCargoLineItem.class
                                            );
                                            cargoLineItem.setCargoItemID(cargoItemID);
                                            return cargoLineItem;
                                        });
                            });
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
                })
                .buffer(SQL_LIST_BUFFER_SIZE)
                .concatMap(cargoLineItemService::createAll)
                .then();
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

    private Flux<Seal> processSeals(ShippingInstructionUpdateInfo shippingInstructionUpdateInfo,
                                    ShipmentEquipmentTO shipmentEquipmentTO,
                                    boolean mustBeCreate
    ) {
        String equipmentReference = shipmentEquipmentTO.getEquipment().getEquipmentReference();
        UUID shipmentEquipmentID = shippingInstructionUpdateInfo.getShipmentEquipmentIDFor(equipmentReference);
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

    private Mono<ShipmentEquipment> findOrCreateShipmentEquipment(UUID shipmentID, ShipmentEquipmentTO shipmentEquipmentTO) {
        EquipmentTO equipmentTO = shipmentEquipmentTO.getEquipment();
        String equipmentReference = equipmentTO.getEquipmentReference();
        return shipmentEquipmentService.findByEquipmentReference(equipmentReference)
                .switchIfEmpty(Mono.defer(() -> {
                    ShipmentEquipment shipmentEquipment = MappingUtil.instanceFrom(
                            shipmentEquipmentTO,
                            ShipmentEquipment::new,
                            AbstractShipmentEquipment.class
                    );
                    shipmentEquipment.setShipmentID(shipmentID);
                    shipmentEquipment.setEquipmentReference(equipmentReference);
                    return shipmentEquipmentService.create(shipmentEquipment);
                }));
    }

    private Mono<Tuple2<Map<String, UUID>, List<ShipmentEquipmentTO>>> updateShipmentEquipment(
            ShippingInstructionUpdateInfo shippingInstructionUpdateInfo,
            List<ShipmentEquipmentTO> shipmentEquipmentTOs,
            boolean nullReeferEnsuresAbsence
    ) {
        Map<String, UUID> referenceToDBId = new HashMap<>();
        return Flux.fromIterable(shipmentEquipmentTOs)
                .concatMap(shipmentEquipmentTO -> {
                    EquipmentTO equipmentTO = shipmentEquipmentTO.getEquipment();
                    String equipmentReference = equipmentTO.getEquipmentReference();
                    UUID shipmentID = shippingInstructionUpdateInfo.getShipmentIDForEquipmentReference(equipmentReference);

                    // TODO Performance: 1 Query for each ShipmentEquipment and then 1 for each ActiveReeferSettings
                    // This probably be reduced to one big "LEFT JOIN ... WHERE table.equipmentReference IN (LIST)".
                    return findOrCreateShipmentEquipment(shipmentID, shipmentEquipmentTO)
                            .flatMap(shipmentEquipment -> {
                                referenceToDBId.put(equipmentReference, shipmentEquipment.getId());
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
                                                                        + equipmentReference
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
                                                                        + equipmentReference
                                                                        + ": The equipment does not have an active reefer."
                                                        ))
                                        ).flatMap(current -> {
                                            ActiveReeferSettings update = shipmentEquipmentTO.getActiveReeferSettings();
                                            if (update.getShipmentEquipmentID() != null) {
                                                return Mono.error(new CreateException(
                                                        "Cannot modify ActiveReeferSettings on "
                                                                + equipmentReference
                                                                + ": Please omit shipmentEquipmentID (it is implicit)"));
                                            }
                                            return activeReeferSettingsService.update(update);
                                        });
                            });
                })
                .doOnNext(ignored -> shippingInstructionUpdateInfo.setEquipmentReference2ShipmentEquipmentID(Collections.unmodifiableMap(referenceToDBId)))
                .then(Mono.zip(Mono.just(referenceToDBId), Mono.just(shipmentEquipmentTOs)));
    }

    private Mono<Void> mapParties(UUID shippingInstructionID, Iterable<DocumentPartyTO> documentPartyTOs,
                                  Function<DocumentParty, Mono<DocumentParty>> saveFunction) {
        return Flux.fromIterable(documentPartyTOs)
                .concatMap(documentPartyTO -> {
                    DocumentParty documentParty;
                    Party party = documentPartyTO.getParty();
                    Mono<Party> partyMono;

                    documentParty = MappingUtil.instanceFrom(documentPartyTO, DocumentParty::new, AbstractDocumentParty.class);
                    documentParty.setShippingInstructionID(shippingInstructionID);

                    partyMono = Util.resolveModelReference(party, partyService::findById, partyService::create, "Party");

                    return partyMono
                            .doOnNext(resolvedParty -> {
                                documentParty.setPartyID(resolvedParty.getId());
                                documentPartyTO.setParty(resolvedParty);
                            }).flatMap(resolvedParty -> saveFunction.apply(documentParty));
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
        try {
            shippingInstructionTO.pushCarrierBookingReferenceIntoCargoItemsIfNecessary();
        } catch (IllegalStateException e) {
            return Mono.error(new CreateException("Detected carrierBookingReference on the ShippingInstruction AND on"
                    + " the CargoItems.  Please place them *either* on the ShippingInstruction (if they are all"
                    + " identical) OR place them entirely on the CargoItem level (if you need distinct values)."
            ));
        }

        return equipmentService.ensureEquipmentExistAndMatchesRequest(shippingInstructionTO.getShipmentEquipments())
                .then(processFreightPayableAt(shippingInstructionTO, shippingInstruction))
                .thenReturn(shippingInstruction)
                .flatMap(shippingInstructionService::create)
                .flatMapMany(savedShippingInstruction -> {
            UUID shippingInstructionID = savedShippingInstruction.getId();
            shippingInstructionTO.setId(savedShippingInstruction.getId());
            ShippingInstructionUpdateInfo shippingInstructionUpdateInfo = new ShippingInstructionUpdateInfo(shippingInstructionID, shippingInstructionTO);

            return loadShipmentIDs(shippingInstructionUpdateInfo)
                    .flatMap(ignored -> updateShipmentEquipment(
                            shippingInstructionUpdateInfo,
                            shippingInstructionTO.getShipmentEquipments(),
                            false
                    )).flatMapMany(equipmentTuple ->
                        Flux.concat(
                                Flux.fromIterable(equipmentTuple.getT2())
                                    .concatMap(shipmentEquipmentTO ->
                                            processSeals(shippingInstructionUpdateInfo, shipmentEquipmentTO, true)),
                                processCargoItems(
                                        shippingInstructionUpdateInfo,
                                        shippingInstructionTO.getCargoItems(),
                                        true
                                ),

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
        }).then(Mono.just(shippingInstructionTO))
        // Conclude with a lookup from scratch.  It may seem wasteful it is the
        // Simplest way to get this correct as we can get additional information
        // that were not a part of the POST (e.g. ShipmentLocations via the
        // Shipment).  See #63 and #64 as an example of issues fixed by this
        // approach
        .map(ShippingInstructionTO::getId)
        .flatMap(this::findById);
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
                    try {
                        original.pushCarrierBookingReferenceIntoCargoItemsIfNecessary();
                        update.pushCarrierBookingReferenceIntoCargoItemsIfNecessary();
                    } catch (IllegalStateException e) {
                        return Mono.error(new UpdateException("Detected carrierBookingReference on the ShippingInstruction AND on"
                                + " the CargoItems.  Please place them *either* on the ShippingInstruction (if they are all"
                                + " identical) OR place them entirely on the CargoItem level (if you need distinct values)."
                        ));
                    }

                    checkForDuplicates(update.getCargoItems(), CargoItemTO::getId, "cargoItems[*].cargoItemID");
                    for (CargoItemTO cargoItemTO : update.getCargoItems()) {
                        UUID id = cargoItemTO.getId();
                        String name = id != null ? "found in cargoItem with id " + id.toString() : "found in cargoItem without an id";
                        checkForDuplicates(cargoItemTO.getCargoLineItems(), CargoLineItemTO::getCargoLineItemID, name);
                    }
                    checkForDuplicates(
                            update.getShipmentEquipments(),
                            se -> se.getEquipment().getEquipmentReference(),
                            "shipmentLocations[*].equipment.equipmentReference"
                    );

                    ShippingInstructionUpdateInfo shippingInstructionUpdateInfo = new ShippingInstructionUpdateInfo(shippingInstructionId, update);
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
                    ChangeSet<CargoItemTO> cargoItemTOChangeSet = changeListDetector(
                            original.getCargoItems(),
                            update.getCargoItems(),
                            CargoItemTO::getId,
                            acceptAny()
                    );
                    // CargoLineItemTOs are handled via delete + create.  Creation happens
                    // indirectly via the CargoItem(TO)s.
                    Flux<Tuple2<UUID, List<CargoLineItemTO>>> orphanedCargoLineItemTOs =
                            Flux.fromIterable(original.getCargoItems())
                            .flatMap(cargoItemTO -> {
                                UUID cargoItemID = cargoItemTO.getId();
                                return Mono.zip(
                                        Mono.just(cargoItemID),
                                        Mono.just(cargoItemTO.getCargoLineItems())
                                );
                            });

                    ChangeSet<ShipmentEquipmentTO> shipmentEquipmentTOChangeSet = changeListDetector(
                            original.getShipmentEquipments(),
                            update.getShipmentEquipments(),
                            shipmentEquipmentTO -> Objects.requireNonNull(shipmentEquipmentTO.getEquipment()).getEquipmentReference(),
                            acceptAny(),
                            true
                    );
                    List<CargoItemTO> nonDeletedCargoItems = Stream.concat(
                            cargoItemTOChangeSet.newInstances.stream(),
                            cargoItemTOChangeSet.updatedInstances.stream()
                    ).collect(Collectors.toList());
                    List<ShipmentEquipmentTO> shipmentEquipmentTOs = shipmentEquipmentTOChangeSet.getAllNewAndUpdatedInstances();

                    Flux<?> deleteFirst = Flux.concat(
                            Flux.fromIterable(original.getShipmentEquipments())
                                .concatMapIterable(ShipmentEquipmentTO::getSeals)
                                .doOnNext(seal -> Objects.requireNonNull(seal.getId()))
                                .concatMap(sealService::delete),
                            Flux.fromIterable(original.getReferences())
                                .concatMap(referenceService::delete),
                            documentPartyService.deleteObsoleteDocumentPartyInstances(documentPartyTOChangeSet.orphanedInstances),
                            // We delete obsolete cargo item and cargo line items first.  This avoids conflicts if a
                            // cargo line item is moved between two cargo items (as you can only use the ID once).
                            orphanedCargoLineItemTOs
                                    .flatMap(tuple ->
                                            cargoLineItemService.deleteByCargoItemIDAndCargoLineItemIDIn(
                                                    tuple.getT1(),
                                                    tuple.getT2().stream().map(CargoLineItemTO::getCargoLineItemID).collect(Collectors.toList())
                                            )
                                    ).thenMany(Flux.fromStream(cargoItemTOChangeSet.orphanedInstances.stream().map(CargoItemTO::getId)))
                                    .buffer(SQL_LIST_BUFFER_SIZE)
                                    .concatMap(cargoItemService::deleteAllByIdIn)
                    );

                    Flux<Object> handleEquipmentAndCargoItems =
                            loadShipmentIDs(shippingInstructionUpdateInfo)
                            .thenReturn(shipmentEquipmentTOs)
                            .flatMap(equipmentService::ensureEquipmentExistAndMatchesRequest)
                            .thenReturn(shipmentEquipmentTOs)
                            .flatMap(ignored ->
                                    updateShipmentEquipment(
                                            shippingInstructionUpdateInfo,
                                            shipmentEquipmentTOs,
                                            true
                            )).flatMapMany(equipmentTuple ->
                                Flux.fromIterable(equipmentTuple.getT2())
                                .flatMap(shipmentEquipmentTO ->
                                        processSeals(shippingInstructionUpdateInfo, shipmentEquipmentTO, false))
                                .thenMany(
                                    processCargoItems(
                                            shippingInstructionUpdateInfo,
                                            nonDeletedCargoItems,
                                            false
                                    )
                                // count + flatMap ensures a non-empty mono while trivially deferring the .update call
                                // The alternative .then(Mono.defer(() -> X)) is vastly harder to read.
                                ).count()
                                .flatMap(ignored -> {
                                    if (!shipmentEquipmentTOChangeSet.orphanedInstances.isEmpty()) {
                                        List<String> orphanedReference = shipmentEquipmentTOChangeSet.orphanedInstances
                                                .stream()
                                                .map(ShipmentEquipmentTO::getEquipment)
                                                .map(EquipmentTO::getEquipmentReference)
                                                .collect(Collectors.toList());
                                        return shipmentEquipmentService.deleteByEquipmentReferenceInAndShipmentIDIn(
                                                orphanedReference,
                                                shippingInstructionUpdateInfo.getAllShipmentIDs()
                                        );
                                    }
                                    return Mono.empty();
                                })
                    );

                    Flux<?> deferredUpdates = Flux.concat(
                            handleEquipmentAndCargoItems,
                            processFreightPayableAt(update, updatedModel),
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
                            mapReferences(shippingInstructionId, update.getReferences(), referenceService::create)
                    );

                    return deleteFirst
                            .thenMany(deferredUpdates)
                            // count + flatMap ensures a non-empty mono while trivially deferring the .update call
                            // The alternative .then(Mono.defer(() -> X)) is vastly harder to read.
                            .count()
                            .flatMap(ignored -> shippingInstructionService.update(updatedModel))
                            .thenReturn(update)
                            .doOnNext(ShippingInstructionTO::hoistCarrierBookingReferenceIfPossible);
                });
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

    @Override
    public String getCarrierBookingReference(ShippingInstructionTO shippingInstructionTO) {
        if (shippingInstructionTO.getCarrierBookingReference() != null) {
            // Use the carrierBookingReference on the ShippingInstruction
            return shippingInstructionTO.getCarrierBookingReference();
        } else {
            List<CargoItemTO> cargoItems = shippingInstructionTO.getCargoItems();
            if (cargoItems != null) {
                for (CargoItemTO cargoItem : cargoItems) {
                    if (cargoItem.getCarrierBookingReference() != null) {
                        return cargoItem.getCarrierBookingReference();
                    }
                }
            }
        }
        return null;
    }
}
