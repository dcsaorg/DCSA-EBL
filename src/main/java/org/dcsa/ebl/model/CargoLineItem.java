package org.dcsa.ebl.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.dcsa.core.model.AuditBase;
import org.dcsa.core.model.GetId;
import org.springframework.data.relational.core.mapping.Column;

import java.util.UUID;

@NoArgsConstructor
@Data
public class CargoLineItem extends AuditBase implements GetId<Void> {

    @Column("cargo_line_item_id")
    private String cargoLineItemID;

    @Column("cargo_item_id")
    private UUID cargoItemID;

    @Column("shipping_marks")
    private String shippingMarks;
}
