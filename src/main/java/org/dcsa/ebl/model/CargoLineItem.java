package org.dcsa.ebl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore
    private UUID id;  /* TODO: Remove */

    @Column("cargo_item_id")
    private UUID cargoItemID;
}
