package org.dcsa.ebl.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.dcsa.core.model.GetId;
import org.dcsa.ebl.model.base.AbstractCargoItem;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Table("cargo_item")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class CargoItem extends AbstractCargoItem implements GetId<UUID> {

    @Id
    private UUID id;

    @Column("shipment_id")
    @NotNull
    private UUID shipmentID;

    @Column("shipping_instruction_id")
    private UUID shippingInstructionID;

    @Column("shipment_equipment_id")
    @NotNull
    protected UUID shipmentEquipmentID;
}
