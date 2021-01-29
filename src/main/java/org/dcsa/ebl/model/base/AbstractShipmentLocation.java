package org.dcsa.ebl.model.base;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dcsa.core.model.AuditBase;
import org.dcsa.ebl.model.enums.ShipmentLocationType;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
public class AbstractShipmentLocation extends AuditBase {

    @Column("location_id")
    private UUID locationID;

    @Column("location_type")
    private ShipmentLocationType locationType;

    @Column("displayed_name")
    private String displayedName;

}
