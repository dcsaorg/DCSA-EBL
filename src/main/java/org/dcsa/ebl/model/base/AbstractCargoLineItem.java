package org.dcsa.ebl.model.base;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.dcsa.core.model.AuditBase;
import org.springframework.data.relational.core.mapping.Column;

@NoArgsConstructor
@Data
public class AbstractCargoLineItem extends AuditBase {

    @Column("cargo_line_item_id")
    private String cargoLineItemID;

    @Column("shipping_marks")
    private String shippingMarks;
}
