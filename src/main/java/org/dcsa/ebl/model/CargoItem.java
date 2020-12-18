package org.dcsa.ebl.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.dcsa.ebl.model.base.AbstractCargoItem;
import org.springframework.data.relational.core.mapping.Table;

@Table("cargo_item")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class CargoItem extends AbstractCargoItem {
}
