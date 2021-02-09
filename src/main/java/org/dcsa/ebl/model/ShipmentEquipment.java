package org.dcsa.ebl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.dcsa.core.model.GetId;
import org.dcsa.ebl.model.base.AbstractShipmentEquipment;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.Size;
import java.util.UUID;

@Table("shipment_equipment")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ShipmentEquipment extends AbstractShipmentEquipment implements GetId<UUID> {

    @Id
    @Column("id")
    private UUID id;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Column("shipment_id")
    private UUID shipmentID;

    @Column("equipment_reference")
    @Size(max = 15)
    private String equipmentReference;
}
