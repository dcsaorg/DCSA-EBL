package org.dcsa.ebl.model;

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
    @Id
    private UUID id;

    @Column("shipment_id")
    @NotNull
    private UUID shipmentID;

    @Column("commodity_type")
    @Size(max = 20)
    private String commodityType;

    @Column("shipping_marks")
    private String shippingMarks;

    @Column("description_of_goods")
    private String descriptionOfGoods;

    @Column("hs_code")
    @Size(max = 10)
    private String hsCode;

    private Float weight;

    private Float volume;

    @Column("weight_unit")
    @Size(max = 3)
    private WeightUnit weightUnit;

    public void setWeightUnit(String weightUnit) {
        this.weightUnit = WeightUnit.valueOf(weightUnit);
    }

    @Column("volume_unit")
    @Size(max = 16)
    private VolumeUnit volumeUnit;

    public void setVolumeUnit(String volumeUnit) {
        this.volumeUnit = VolumeUnit.valueOf(volumeUnit);
    }

    @Column("number_of_packages")
    private Integer numberOfPackages;

    @Column("carrier_booking_reference")
    @Size(max = 35)
    private String carrierBookingReference;

    @Column("shipping_instruction_number")
    @Size(max = 20)
    private String shippingInstructionNumber;
}
