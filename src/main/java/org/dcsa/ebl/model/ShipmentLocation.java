package org.dcsa.ebl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dcsa.core.model.AuditBase;
import org.dcsa.core.model.GetId;
import org.dcsa.ebl.model.base.AbstractShipmentLocation;
import org.dcsa.ebl.model.enums.ShipmentLocationType;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("shipment_location")
@Data
@EqualsAndHashCode(callSuper = true)
public class ShipmentLocation extends AbstractShipmentLocation implements GetId<UUID> {

    @Id
    @JsonIgnore
    private UUID id;  /* TODO: Remove */

    @Column("shipment_id")
    private UUID shipmentID;
}
