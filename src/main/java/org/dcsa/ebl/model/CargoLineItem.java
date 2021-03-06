package org.dcsa.ebl.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.dcsa.ebl.model.base.AbstractCargoLineItem;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

import java.util.UUID;

@NoArgsConstructor
@Data
public class CargoLineItem extends AbstractCargoLineItem {

    @Id
    private UUID id;  /* TODO: Remove */

    @Column("cargo_item_id")
    private UUID cargoItemID;
}
