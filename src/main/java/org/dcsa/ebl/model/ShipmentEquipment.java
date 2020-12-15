package org.dcsa.ebl.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.dcsa.core.model.GetId;
import org.dcsa.ebl.model.enums.WeightUnit;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.Size;
import java.util.UUID;

@Table("equipment")
@Data
@NoArgsConstructor
public class ShipmentEquipment implements GetId<UUID> {

    @Id
    private UUID id;

    @Column("shipment_id")
    private UUID shipmentID;

    @Column("equipment_reference")
    @Size(max = 15)
    private String equipmentReference;

    @Column("verified_gross_mass")
    private String verifiedGrossMass;

    @Column("cargo_gross_weight")
    private Float cargoGrossWeight;

    @Column("cargo_gross_weight_unit")
    @Size(max = 3)
    private WeightUnit cargoGrossWeightUnit;

    public void setCargoGrossWeightUnit(String cargoGrossWeightUnit) {
        this.cargoGrossWeightUnit = WeightUnit.valueOf(cargoGrossWeightUnit);
    }

    public void setCargoGrossWeightUnit(WeightUnit cargoGrossWeightUnit) {
        this.cargoGrossWeightUnit = cargoGrossWeightUnit;
    }

}
