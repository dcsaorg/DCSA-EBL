package org.dcsa.ebl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dcsa.core.model.AuditBase;
import org.dcsa.core.model.GetId;
import org.dcsa.ebl.model.enums.ReferenceTypeCode;
import org.springframework.data.relational.core.mapping.Column;

import javax.validation.constraints.Size;
import java.util.UUID;

@NoArgsConstructor
@Data
public class Reference extends AuditBase implements GetId<UUID> {

    @JsonProperty("referenceID")
    @Column("reference_id")
    private UUID referenceID;

    private ReferenceTypeCode referenceType;

    public void setReferenceType(String referenceType) {
        this.referenceType = ReferenceTypeCode.valueOf(referenceType);
    }

    public void setReferenceType(ReferenceTypeCode referenceType) {
        this.referenceType = referenceType;
    }

    @Size(max = 100)
    private String referenceValue;

    @Column("shipping_instruction_id")
    private UUID shippingInstructionID;

    @JsonIgnore
    public UUID getId() {
        return getReferenceID();
    }
}