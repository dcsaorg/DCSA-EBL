package org.dcsa.ebl.model.base;

import lombok.Data;
import org.dcsa.ebl.model.enums.WeightUnit;
import org.springframework.data.relational.core.mapping.Column;

@Data
public abstract class AbstractShipmentEquipment {

    @Column("cargo_gross_weight")
    private Float cargoGrossWeight;

    @Column("cargo_gross_weight_unit")
    private WeightUnit cargoGrossWeightUnit;
}
