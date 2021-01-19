package org.dcsa.ebl.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.dcsa.core.model.AuditBase;
import org.dcsa.core.model.GetId;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("carrier_clauses")
@NoArgsConstructor
@Data
public class Clause extends AuditBase implements GetId<UUID> {
    @Id
    private UUID id;

    @Column("clause_content")
    private String clauseContent;
}
