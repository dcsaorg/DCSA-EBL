package org.dcsa.ebl.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.dcsa.ebl.model.base.AbstractCargoItem;
import org.springframework.data.relational.core.mapping.Table;

@Table("cargo_item")
@Data
@NoArgsConstructor
public class CargoItem extends AbstractCargoItem {
}
