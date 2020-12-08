package org.dcsa.ebl.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dcsa.core.model.AuditBase;
import org.dcsa.core.model.GetId;
import org.dcsa.ebl.model.enums.ServiceType;
import org.dcsa.ebl.model.enums.ShipmentTerm;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Table("booking")
@Data
@NoArgsConstructor
public class Booking extends AuditBase implements GetId<String> {
    @JsonProperty("carrierBookingReference")
    @Column("carrier_booking_reference")
    @Size(max = 35)
    @Id
    private String id;

    @Column("service_type_at_origin")
    @Size(max = 5)
    @NotNull
    private ServiceType serviceTypeAtOrigin;
    
    public void setServiceTypeAtOrigin(@NotNull String serviceTypeAtOrigin) {
        this.serviceTypeAtOrigin = ServiceType.valueOf(serviceTypeAtOrigin);
    }

    public void setServiceTypeAtOrigin(@NotNull ServiceType serviceTypeAtOrigin) {
        this.serviceTypeAtOrigin = serviceTypeAtOrigin;
    }

    @Column("service_type_at_destination")
    @Size(max = 5)
    @NotNull
    private ServiceType serviceTypeAtDestination;

    public void setServiceTypeAtDestination(@NotNull String serviceTypeAtDestination) {
        this.serviceTypeAtDestination = ServiceType.valueOf(serviceTypeAtDestination);
    }

    public void setServiceTypeAtDestination(@NotNull ServiceType serviceTypeAtDestination) {
        this.serviceTypeAtDestination = serviceTypeAtDestination;
    }

    @Column("shipment_term_at_origin")
    @Size(max = 5)
    @NotNull
    private ShipmentTerm shipmentTermAtOrigin;

    public void setShipmentTermAtOrigin(@NotNull String shipmentTermAtOrigin) {
        this.shipmentTermAtOrigin = ShipmentTerm.valueOf(shipmentTermAtOrigin);
    }

    public void setShipmentTermAtOrigin(@NotNull ShipmentTerm shipmentTermAtOrigin) {
        this.shipmentTermAtOrigin = shipmentTermAtOrigin;
    }

    @Column("shipment_term_at_destination")
    @Size(max = 5)
    @NotNull
    private ShipmentTerm shipmentTermAtDestination;

    public void setShipmentTermAtDestination(@NotNull String shipmentTermAtDestination) {
        this.shipmentTermAtDestination = ShipmentTerm.valueOf(shipmentTermAtDestination);
    }

    public void setShipmentTermAtDestination(@NotNull ShipmentTerm shipmentTermAtDestination) {
        this.shipmentTermAtDestination = shipmentTermAtDestination;
    }

    @Column("booking_datetime")
    @NotNull
    private LocalDateTime bookingDatetime;

    @Column("service_contract")
    @Size(max = 30)
    @NotNull
    private String serviceContract;

    @Column("commodity_type")
    @NotNull
    @Size(max = 20)
    private String commodityType;

    @Column("cargo_gross_weight")
    @NotNull
    private Double cargoGrossWeight;

    @Column("cargo_gross_weight_unit")
    @Size(max = 3)
    @NotNull
    private String cargoGrossWeightUnit;
}

