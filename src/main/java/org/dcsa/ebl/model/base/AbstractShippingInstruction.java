package org.dcsa.ebl.model.base;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.dcsa.core.model.AuditBase;
import org.dcsa.core.model.GetId;
import org.dcsa.ebl.model.enums.TransportDocumentTypeCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public abstract class AbstractShippingInstruction extends AuditBase implements GetId<UUID> {

    @Id
    @JsonProperty("shippingInstructionID")
    private UUID id;

    @Column("number_of_copies")
    private Integer numberOfCopies;

    @Column("number_of_originals")
    private Integer numberOfOriginals;

    @Column("is_electronic")
    private Boolean isElectronic;

    @Column("is_shipped_onboard_type")
    @NotNull
    private Boolean isShippedOnBoardType;

    @Column("transport_document_type")
    @NotNull
    private TransportDocumentTypeCode transportDocumentType;

    @Column("is_charges_displayed")
    private Boolean isChargesDisplayed;
}
