package org.dcsa.ebl.model.base;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.dcsa.ebl.model.enums.WeightUnit;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

import javax.validation.constraints.Size;
import java.util.UUID;

@Data
public abstract class AbstractShipmentEquipment {

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
