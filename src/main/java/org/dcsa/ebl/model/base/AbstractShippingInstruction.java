package org.dcsa.ebl.model.base;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.dcsa.core.model.AuditBase;
import org.dcsa.ebl.model.enums.TransportDocumentTypeCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public abstract class AbstractShippingInstruction extends AuditBase {

    @Id
    @Column("id")
    private String shippingInstructionID;

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
