package org.dcsa.ebl.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dcsa.core.model.AuditBase;
import org.dcsa.core.model.GetId;
import org.dcsa.ebl.model.enums.ServiceType;
import org.dcsa.ebl.model.enums.ShipmentTerm;
import org.dcsa.ebl.model.enums.WeightUnit;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Table("booking")
@NoArgsConstructor
@Data
public class Booking extends AuditBase implements GetId<String> {

    @Id
    @Column("carrier_booking_reference")
    @JsonProperty("carrierBookingReference")
    @Size(max = 35)
    private String id;

    @Column("service_type_at_origin")
    @Size(max = 3)
    private ServiceType serviceTypeAtOrigin;

    public void setServiceTypeAtOrigin(String serviceTypeAtOrigin) {
        this.serviceTypeAtOrigin = ServiceType.valueOf(serviceTypeAtOrigin);
    }

    public void setServiceTypeAtOrigin(ServiceType serviceTypeAtOrigin) {
        this.serviceTypeAtOrigin = serviceTypeAtOrigin;
    }

    @Column("service_type_at_destination")
    @Size(max = 3)
    private ServiceType serviceTypeAtDestination;

    public void setServiceTypeAtDestination(String serviceTypeAtDestination) {
        this.serviceTypeAtDestination = ServiceType.valueOf(serviceTypeAtDestination);
    }

    public void setServiceTypeAtDestination(ServiceType serviceTypeAtDestination) {
        this.serviceTypeAtDestination = serviceTypeAtDestination;
    }

    @Column("shipment_term_at_origin")
    @Size(max = 3)
    private ShipmentTerm shipmentTermAtOrigin;

    public void setShipmentTermAtOrigin(String shipmentTermAtOrigin) {
        this.shipmentTermAtOrigin = ShipmentTerm.valueOf(shipmentTermAtOrigin);
    }

    public void setShipmentTermAtOrigin(ShipmentTerm shipmentTermAtOrigin) {
        this.shipmentTermAtOrigin = shipmentTermAtOrigin;
    }

    @Column("shipment_term_at_destination")
    @Size(max = 3)
    private ShipmentTerm shipmentTermAtDestination;

    public void setShipmentTermAtDestination(String shipmentTermAtDestination) {
        this.shipmentTermAtDestination = ShipmentTerm.valueOf(shipmentTermAtDestination);
    }

    public void setShipmentTermAtDestination(ShipmentTerm shipmentTermAtDestination) {
        this.shipmentTermAtDestination = shipmentTermAtDestination;
    }

    @Column("booking_datetime")
    private ZonedDateTime bookingDateTime;

    @Column("service_contract")
    @Size(max = 30)
    private String serviceContract;

    @Column("commodity_type")
    @Size(max = 20)
    private String commodityType;

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
