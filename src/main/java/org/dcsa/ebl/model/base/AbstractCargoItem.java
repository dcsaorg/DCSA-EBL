package org.dcsa.ebl.model.base;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.dcsa.core.model.AuditBase;
import org.dcsa.ebl.model.enums.VolumeUnit;
import org.dcsa.ebl.model.enums.WeightUnit;
import org.springframework.data.relational.core.mapping.Column;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public abstract class AbstractCargoItem extends AuditBase {


    @Column("description_of_goods")
    @NotNull
    private String descriptionOfGoods;

    @JsonProperty("HSCode")
    @Column("hs_code")
    @Size(max = 10)
    @NotNull
    private String hsCode;

    @Column("number_of_packages")
    private Integer numberOfPackages;

    private Float weight;

    private Float volume;

    @Column("weight_unit")
    private WeightUnit weightUnit;

    public void setWeightUnit(String weightUnit) {
        this.weightUnit = WeightUnit.valueOf(weightUnit);
    }

    public void setWeightUnit(WeightUnit weightUnit) {
        this.weightUnit = weightUnit;
    }

    @Column("volume_unit")
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

}
