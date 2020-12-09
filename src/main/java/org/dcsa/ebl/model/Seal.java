package org.dcsa.ebl.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.dcsa.core.model.AuditBase;
import org.dcsa.core.model.GetId;
import org.springframework.data.relational.core.mapping.Column;

import javax.validation.constraints.Size;

@NoArgsConstructor
@Data
public class Seal extends AuditBase implements GetId<UUID> {
    @Column("seal_number")
    @Size(max = 15)
    private String sealNumber;

    @Column("seal_source")
    @Size(max = 5)
    private String sealSource;

    @Column("seal_type")
    @Size(max = 5)
    private String sealType;
}
