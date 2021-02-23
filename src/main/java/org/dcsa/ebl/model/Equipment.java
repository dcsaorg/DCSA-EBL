package org.dcsa.ebl.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.dcsa.ebl.model.base.AbstractEquipment;
import org.springframework.data.relational.core.mapping.Table;

@Table("equipment")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Equipment extends AbstractEquipment {

}
