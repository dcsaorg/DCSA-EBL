package org.dcsa.ebl.model.transferobjects;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.dcsa.ebl.model.Equipment;
import org.dcsa.ebl.model.base.AbstractEquipment;
import org.dcsa.ebl.model.utils.MappingUtil;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class EquipmentTO extends AbstractEquipment {

    public boolean containsOnlyID() {
        if (this.getEquipmentReference() != null) {
            EquipmentTO p = new EquipmentTO();
            p.setEquipmentReference(this.getEquipmentReference());
            return this.equals(p);
        }
        return false;
    }

    public Equipment createModifiedCopyOrNull(Equipment equipment) {
        Equipment clone ;
        boolean modified = false;
        if (this.containsOnlyID()) {
            return null;
        }
        clone = MappingUtil.instanceFrom(equipment, Equipment::new, Equipment.class);
        assert this.getEquipmentReference().equals(equipment.getEquipmentReference());

        modified |= setIfChanged(clone.getWeightUnit(), this.getWeightUnit(), clone::setWeightUnit);
        modified |= setIfChanged(clone.getTareWeight(), this.getTareWeight(), clone::setTareWeight);
        modified |= setIfChanged(clone.getIsoEquipmentCode(), this.getIsoEquipmentCode(), clone::setIsoEquipmentCode);
        if (this.getIsShipperOwned() != null) {
            modified |= setIfChanged(clone.getIsShipperOwned(), this.getIsShipperOwned(), clone::setIsShipperOwned);
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
