package org.dcsa.ebl.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dcsa.core.model.GetId;
import org.dcsa.ebl.model.base.AbstractClause;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("carrier_clauses")
@Data
@EqualsAndHashCode(callSuper = true)
public class Clause extends AbstractClause implements GetId<UUID> {

}
