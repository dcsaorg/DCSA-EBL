package org.dcsa.ebl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.dcsa.ebl.model.base.AbstractCargoItem;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Table("cargo_item")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class CargoItem extends AbstractCargoItem {

    @JsonIgnore
    @Column("shipment_id")
    @NotNull
    private UUID shipmentID;

    @Column("shipping_instruction_id")
    private UUID shippingInstructionID;
}
