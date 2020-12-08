package org.dcsa.ebl.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dcsa.core.model.AuditBase;
import org.dcsa.core.model.GetId;
import org.dcsa.ebl.model.enums.WeightUnit;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.Size;

@Table("equipment")
@Data
@NoArgsConstructor
public class Equipment extends AuditBase implements GetId<String> {
    @Id
    @JsonProperty("equipmentReference")
    @Column("equipment_reference")
    @Size(max = 15)
    private String id;

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
