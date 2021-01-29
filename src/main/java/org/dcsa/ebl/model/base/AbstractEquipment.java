package org.dcsa.ebl.model.base;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dcsa.core.model.AuditBase;
import org.dcsa.ebl.model.enums.WeightUnit;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

import javax.validation.constraints.Size;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractEquipment extends AuditBase {

    @Id
    @Column("equipment_reference")
    @Size(max = 15)
    private String equipmentReference;

    @Column("iso_equipment_code")
    @Size(max = 4)
    private String isoEquipmentCode;

    @Column("tare_weight")
    private Float tareWeight;

    @Column("weight_unit")
    @Size(max = 3)
    private WeightUnit weightUnit;

    public void setWeightUnit(String weightUnit) {
        this.weightUnit = WeightUnit.valueOf(weightUnit);
    }

    public void setWeightUnit(WeightUnit weightUnit) {
        this.weightUnit = weightUnit;
    }

}
