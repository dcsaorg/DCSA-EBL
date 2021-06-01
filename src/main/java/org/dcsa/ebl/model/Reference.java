package org.dcsa.ebl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.dcsa.core.model.AuditBase;
import org.dcsa.core.model.GetId;
import org.dcsa.ebl.model.enums.ReferenceTypeCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.Size;
import java.util.UUID;

// REFERENCES is an SQL keyword (as well as the name of the table)
// Use magic quotes to work around the name clash.
@Table("\"references\"")
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class Reference extends AuditBase implements GetId<UUID> {

    @Id
    @Column("id")
    @JsonIgnore
    private UUID referenceID;

    private ReferenceTypeCode referenceType;

    @Size(max = 100)
    private String referenceValue;

    @Column("shipping_instruction_id")
    @JsonIgnore
    private String shippingInstructionID;

    @JsonIgnore
    public UUID getId() {
        return getReferenceID();
    }
}
