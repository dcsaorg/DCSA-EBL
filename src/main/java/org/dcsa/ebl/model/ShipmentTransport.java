package org.dcsa.ebl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

@Table("shipment_transport")
@Data
@NoArgsConstructor
public class ShipmentTransport extends AuditBase implements GetId<UUID> {

    @Id
    @JsonIgnore
    private UUID id;  /* TODO: Remove */

    @Column("shipment_id")
    private UUID shipmentID;

    @Column("transport_id")
    @NotNull
    private UUID transportID;

    @Column("sequence_number")
    @NotNull
    private Integer sequenceNumber;

    @Column("commercial_voyage_id")
    private UUID commercialVoyageID;
}
