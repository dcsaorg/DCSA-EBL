package org.dcsa.ebl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dcsa.core.model.AuditBase;
import org.dcsa.core.model.GetId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;

import java.util.UUID;

@NoArgsConstructor
@Data
public class CargoLineItem extends AuditBase implements GetId<UUID> {

    @Id
    @JsonIgnore
    private UUID id;  /* TODO: Remove */

    @Column("cargo_line_item_id")
    private String cargoLineItemID;

    @Column("cargo_item_id")
    private UUID cargoItemID;

    @Column("shipping_marks")
    private String shippingMarks;
}
