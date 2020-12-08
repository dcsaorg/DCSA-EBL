package org.dcsa.ebl.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dcsa.core.model.AuditBase;
import org.dcsa.core.model.GetId;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

@Table("shipment")
@Data
@NoArgsConstructor
public class Shipment extends AuditBase implements GetId<UUID> {

    @Id
    @JsonProperty("shipmentID")
    private UUID id;

    @Column("collection_datetime")
    @NotNull
    private LocalDateTime collectionDatetime;

    @Column("delivery_datetime")
    @NotNull
    private LocalDateTime deliveryDatetime;

    @Column("carrier_id")
    private UUID carrierID;

    @Column("carrier_booking_reference")
    @Size(max = 35)
    @NotNull
    private String carrierBookingReference;
}
