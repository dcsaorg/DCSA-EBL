package org.dcsa.ebl.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dcsa.core.model.AuditBase;
import org.dcsa.core.model.GetId;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
public class Booking extends AuditBase implements GetId<String> {
    @JsonProperty("carrierBookingReference")
    @Column("carrier_booking_reference")
    @Size(max = 35)
    @Id
    private String id;

    @Column("shipment_id")
    private UUID shipmentID;

    @Column("service_type_at_origin")
    @Size(max = 5)
    private String serviceTypeAtOrigin;

    @Column("service_type_at_destination")
    @Size(max = 5)
    private String serviceTypeAtDestination;

    @Column("shipment_term_at_origin")
    @Size(max = 5)
    private String shipmentTermAtOrigin;

    @Column("shipment_term_at_destination")
    @Size(max = 5)
    private String shipmentTermAtDestination;

    @Column("booking_datetime")
    private LocalDateTime bookingDatetime;

    @Column("service_contract")
    @Size(max = 30)
    private String serviceContract;
}
