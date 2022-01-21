package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.events.model.Equipment;
import org.dcsa.core.events.repository.EquipmentRepository;
import org.dcsa.core.exception.CreateException;
import org.dcsa.core.exception.UpdateException;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.dcsa.ebl.model.base.AbstractEquipment;
import org.dcsa.ebl.model.transferobjects.EquipmentTO;
import org.dcsa.ebl.model.transferobjects.ShipmentEquipmentTO;
import org.dcsa.ebl.model.utils.MappingUtil;
import org.dcsa.ebl.service.EquipmentService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class EquipmentServiceImpl extends ExtendedBaseServiceImpl<EquipmentRepository, Equipment, String> implements EquipmentService {
    private final EquipmentRepository equipmentRepository;

    @Override
    public EquipmentRepository getRepository() {
        return equipmentRepository;
    }

    @Override
    public Mono<Void> ensureEquipmentExistAndMatchesRequest(Iterable<ShipmentEquipmentTO> shipmentEquipmentTOs) {
        return Mono.empty();
        // TODO: fix me
//        return Flux.fromIterable(shipmentEquipmentTOs)
//                .flatMap(shipmentEquipmentTO -> {
//                    EquipmentTO equipmentTO = shipmentEquipmentTO.getEquipment();
//                    String equipmentReference = equipmentTO.getEquipmentReference();
//                    return equipmentRepository.findById(equipmentReference)
//                            .flatMap(resolvedEquipment -> {
//                                Equipment updatedVersion = equipmentTO.createModifiedCopyOrNull(resolvedEquipment);
//                                if (updatedVersion != null) {
//                                    if (!resolvedEquipment.getIsShipperOwned()) {
//                                        return Mono.error(new UpdateException("Cannot modify Carrier owned Equipment (via reference): " + equipmentReference));
//                                    }
//                                    if (!Boolean.TRUE.equals(updatedVersion.getIsShipperOwned())) {
//                                        return Mono.error(new UpdateException("Cannot turn Shipper owner Equipment "
//                                                + equipmentReference + " into a Carrier owned Equipment"
//                                                + " (\"isShipperOwned\" must be true)"
//                                        ));
//                                    }
//                                    return preUpdateHook(resolvedEquipment, updatedVersion)
//                                            .flatMap(this::save);
//                                }
//                                return Mono.just(resolvedEquipment);
//                            }).switchIfEmpty(Mono.defer(() -> {
//                                Equipment equipment;
//                                if (equipmentTO.isSolelyReferenceToModel()) {
//                                    return Mono.error(new CreateException("Unknown Equipment reference: " + equipmentReference));
//                                }
//                                if (!Boolean.TRUE.equals(equipmentTO.getIsShipperOwned())) {
//                                    return Mono.error(new CreateException("Unknown Equipment reference: " + equipmentReference
//                                            + " (set \"isShipperOwned\" to true to create it if it is your own container)"));
//                                }
//                                equipment = MappingUtil.instanceFrom(equipmentTO, Equipment::new, AbstractEquipment.class);
//                                equipment.setIsShipperOwned(Boolean.TRUE);
//                                return equipmentRepository.insert(equipment);
//                            })).zipWith(Mono.just(shipmentEquipmentTO));
//                }).doOnNext(tuple -> {
//                    Equipment equipment = tuple.getT1();
//                    ShipmentEquipmentTO shipmentEquipmentTO = tuple.getT2();
//                    EquipmentTO newEquipmentTO = MappingUtil.instanceFrom(equipment, EquipmentTO::new, AbstractEquipment.class);
//                    shipmentEquipmentTO.setEquipment(newEquipmentTO);
//                }).then();
    }
}
