package org.dcsa.ebl.model.base;

import lombok.Data;
import org.dcsa.ebl.model.enums.WeightUnit;
import org.springframework.data.relational.core.mapping.Column;

import javax.validation.constraints.Size;

@Data
public abstract class AbstractShipmentEquipment {

    @Column("equipment_reference")
    @Size(max = 15)
    private String equipmentReference;

    @Column("verified_gross_mass")
    private String verifiedGrossMass;

    @Column("cargo_gross_weight")
    private Float cargoGrossWeight;

    @Column("cargo_gross_weight_unit")
    private WeightUnit cargoGrossWeightUnit;

    public void setCargoGrossWeightUnit(String cargoGrossWeightUnit) {
        this.cargoGrossWeightUnit = WeightUnit.valueOf(cargoGrossWeightUnit);
    }

    public void setCargoGrossWeightUnit(WeightUnit cargoGrossWeightUnit) {
        this.cargoGrossWeightUnit = cargoGrossWeightUnit;
    }
}
