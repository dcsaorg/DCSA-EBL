package org.dcsa.ebl.service;

import org.dcsa.core.events.model.Equipment;
import org.dcsa.core.service.ExtendedBaseService;
import org.dcsa.ebl.model.transferobjects.EquipmentTO;
import org.dcsa.ebl.model.transferobjects.ShipmentEquipmentTO;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

public interface EquipmentService extends ExtendedBaseService<Equipment, String> {

    /**
     * Verifies and potentially updates Equipment entities to match.
     *
     * When the deferred Mono completes successfully, then the EquipmentTO referenced by the
     * provided ShipmentEquipmentTO objects all exist.  Note that the EquipmentTO <i>may</i>
     * have been replaced (via {@link ShipmentEquipmentTO#setEquipment(EquipmentTO)}) to
     * fill in omitted information about the Equipment instances.
     *
     * Note that:
     * <ul>
     *     <li>
     *         If a EquipmentTO consists <i>only</i> of a equipment reference ({@link EquipmentTO#containsOnlyID()}
     *         is true), then it is <i>always</i> considered a lookup for an existing Equipment (regardless of
     *         ownership).  In this case, the mono will signal an error if an Equipment instance with this reference
     *         cannot be found.
     *     </li>
     *     <li>
     *         If the EquipmentTO references a Carrier owned Container and {@link EquipmentTO#containsOnlyID()} is
     *         false, then the underlying Equipment is<b>never</b> updated. If the EquipmentTO has conflicting values
     *         for the Equipment, the Mono will signal an error.
     *     </li>
     *     <li>
     *         If the EquipmentTO references a Shipper owned Container and ({@link EquipmentTO#containsOnlyID()} is
     *         false, then {@link EquipmentTO#getIsShipperOwned()} must return TRUE (or the mono will signal an error).
     *         Any other field is then considered an update and will be persisted in the {@link EquipmentRepository}.
     *     </li>
     *     <li>
     *         If the Mono signals an error, a subset of the EquipmentTO objects on the input <i>may</i> have been
     *         updated already and will not be undone.  The method relies on Transactions to undo persisting changes
     *         to the underlying data store.
     *     </li>
     * </ul>
     *
     *
     *
     * @param shipmentEquipmentTOs ShipmentEquipmentTO objects from the request.  Note that the
     *                             EquipmentTO object on each ShipmentEquipmentTO object <i>may</i>
     *                             be replaced as a part of this call (e.g. to fill in omitted
     *                             information).
     * @return An empty mono that signals when the process is complete.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    Mono<Void> ensureEquipmentExistAndMatchesRequest(Iterable<ShipmentEquipmentTO> shipmentEquipmentTOs);
}
