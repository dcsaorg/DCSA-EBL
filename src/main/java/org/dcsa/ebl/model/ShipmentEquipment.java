package org.dcsa.ebl.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.dcsa.core.model.GetId;
import org.dcsa.ebl.model.base.AbstractShipmentEquipment;
import org.dcsa.ebl.model.enums.WeightUnit;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.Size;
import java.util.UUID;

@Table("equipment")
@Data
@NoArgsConstructor
public class ShipmentEquipment extends AbstractShipmentEquipment implements GetId<UUID> {

    @Id
    private UUID id;

    @Column("shipment_id")
    private UUID shipmentID;

}
