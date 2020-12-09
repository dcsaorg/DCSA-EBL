package org.dcsa.ebl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dcsa.core.model.AuditBase;
import org.dcsa.core.model.GetId;
import org.dcsa.ebl.model.enums.VolumeUnit;
import org.dcsa.ebl.model.enums.WeightUnit;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.UUID;

@Table("cargo_item")
@Data
@NoArgsConstructor
public class CargoItem extends AuditBase implements GetId<UUID> {
    @JsonProperty("cargoItemID")
    @Id
    private UUID id;

    @JsonIgnore
    @Column("shipment_id")
    @NotNull
    private UUID shipmentID;

    @Column("description_of_goods")
    @NotNull
    private String descriptionOfGoods;

    @Column("hs_code")
    @Size(max = 10)
    @NotNull
    private String hsCode;

    @Column("number_of_packages")
    private Integer numberOfPackages;

    private Float weight;

    private Float volume;

    @Column("weight_unit")
    @Size(max = 3)
    private WeightUnit weightUnit;

    public void setWeightUnit(String weightUnit) {
        this.weightUnit = WeightUnit.valueOf(weightUnit);
    }

    public void setWeightUnit(WeightUnit weightUnit) {
        this.weightUnit = weightUnit;
    }

    @Column("volume_unit")
    @Size(max = 16)
    private VolumeUnit volumeUnit;

    public void setVolumeUnit(String volumeUnit) {
        this.volumeUnit = VolumeUnit.valueOf(volumeUnit);
    }

    public void setVolumeUnit(VolumeUnit volumeUnit) {
        this.volumeUnit = volumeUnit;
    }

    @Column("package_code")
    @Size(max = 3)
    private String packageCode;

    @Column("shipment_equipment_id")
    @NotNull
    private UUID shipmentEquipmentID;
}
