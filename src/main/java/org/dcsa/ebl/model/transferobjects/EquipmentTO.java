package org.dcsa.ebl.model.transferobjects;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.dcsa.ebl.model.base.AbstractEquipment;

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
}
