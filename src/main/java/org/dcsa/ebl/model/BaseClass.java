package org.dcsa.ebl.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.dcsa.core.model.AuditBase;
import org.dcsa.core.model.GetId;
import org.dcsa.ebl.model.enums.*;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.UUID;

@Table("shipping_instruction")
@Data
@NoArgsConstructor
public abstract class BaseClass extends AuditBase implements GetId<UUID> {

    @NotNull
    @Size(max = 35)
    private String carrierBookingReference;

    @NotNull
    private TransportDocumentTypeCode transportDocumentTypeCode;

    @NotNull
    @Size(max = 50)
    private String carrierVoyageNumber;

    @NotNull
    @Size(max = 2000)
    private String descriptionOfGoods;

    @NotNull
    private Party[] parties;

    @NotNull
    private Location[] locations;

    @NotNull
    private ServiceType serviceType;

    @NotNull
    private ShipmentTerm shipmentTerm;

    @NotNull
    private Integer weight;

    @NotNull
    private Integer volume;

    @NotNull
    private WeightUnit weightUnit;

    @NotNull
    private VolumeUnit volumeUnit;

    @NotNull
    private UUID freightPayableAt;

    @NotNull
    @Size(max = 15)
    private String equipmentReference;

    @NotNull
    @Size(max = 4)
    private String ISOEquipmentCode;

    @NotNull
    @Size(max = 3)
    private String paymentTerm;

    @NotNull
    private Integer shipmentEquipmentQuantity;

    @NotNull
    @Size(max = 20)
    private String documentReferenceNumber;

    private Integer numberOfOriginals;

    @Size(max = 15)
    private String sealNumber;

    private Integer sealSource;

    private Integer tareWeight;

    private String clauseContent;

    private Integer declaredValue;

    @Size(max = 3)
    private String declaredValueCurrencyCode;

    private String shippingMarks;

    private Integer exportReferenceNumber;

    @Size(max = 10)
    private String carrierCode;

    @Size(max = 3)
    private String currencyCode;

    private Integer currencyAmount;

    @Size(max = 30)
    private String SVCContract;
}
