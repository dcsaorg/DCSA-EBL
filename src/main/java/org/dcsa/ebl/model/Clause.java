package org.dcsa.ebl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dcsa.core.model.AuditBase;
import org.dcsa.core.model.GetId;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("carrier_clauses")
@Data
@EqualsAndHashCode(callSuper = true)
public class Clause extends AuditBase implements GetId<UUID> {
    @Id
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private UUID id;

    @Column("clause_content")
    private String clauseContent;
}
