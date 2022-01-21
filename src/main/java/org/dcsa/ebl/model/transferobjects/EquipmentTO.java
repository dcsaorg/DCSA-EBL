package org.dcsa.ebl.model.transferobjects;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.dcsa.core.events.model.Equipment;
import org.dcsa.ebl.model.base.AbstractEquipment;
import org.dcsa.ebl.model.utils.MappingUtil;

import java.util.Objects;
import java.util.function.Consumer;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class EquipmentTO extends AbstractEquipment implements ModelReferencingTO<Equipment, String> {

    public boolean isSolelyReferenceToModel() {
        // Cannot rely on Util.containsOnlyID as it does not implement SetId
        // by design
        String equipmentReference = this.getEquipmentReference();
        if (equipmentReference != null) {
            EquipmentTO p = new EquipmentTO();
            p.setEquipmentReference(equipmentReference);
            return this.equals(p);
        }
        return false;
    }

    public boolean isEqualsToModel(Equipment equipment) {
        throw new UnsupportedOperationException("Not implemented (we always use createModifiedCopyOrNull instead)");
    }

    public Equipment createModifiedCopyOrNull(Equipment equipment) {
        Equipment clone ;
        boolean modified = false;
        if (this.isSolelyReferenceToModel()) {
            return null;
        }
        clone = MappingUtil.instanceFrom(equipment, Equipment::new, Equipment.class);
        assert this.getEquipmentReference().equals(equipment.getEquipmentReference());

        // TODO: fix me
//        modified |= setIfChanged(clone.getWeightUnit(), this.getWeightUnit(), clone::setWeightUnit);
        modified |= setIfChanged(clone.getTareWeight(), this.getTareWeight(), clone::setTareWeight);
        modified |= setIfChanged(clone.getIsoEquipmentCode(), this.getIsoEquipmentCode(), clone::setIsoEquipmentCode);
        if (this.getIsShipperOwned() != null) {
        // TODO: fix me
//            modified |= setIfChanged(clone.getIsShipperOwned(), this.getIsShipperOwned(), clone::setIsShipperOwned);
        }

        return modified ? clone : null;
    }

    private static <T> boolean setIfChanged(T original, T update, Consumer<T> setter) {
        if (!Objects.equals(original, update)) {
            setter.accept(update);
            return true;
        }
        return false;
    }
}
