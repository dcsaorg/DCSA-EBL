package org.dcsa.ebl.model.base;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.dcsa.core.model.AuditBase;
import org.dcsa.ebl.model.enums.TransportDocumentTypeCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public abstract class AbstractShippingInstruction extends AuditBase {

    @Id
    @Column("id")
    private String shippingInstructionID;

    @Column("is_shipped_onboard_type")
    @NotNull
    private Boolean isShippedOnBoardType;

    @Column("number_of_copies")
    private Integer numberOfCopies;

    @Column("number_of_originals")
    private Integer numberOfOriginals;

    @Column("is_electronic")
    private Boolean isElectronic;

    @Column("are_charges_displayed_on_originals")
    @NotNull
    private Boolean areChargesDisplayedOnOriginals;

    @Column("are_charges_displayed_on_copies")
    @NotNull
    @Size(max = 100)
    private Boolean areChargesDisplayedOnCopies;

    @Column("place_of_issue")
    private String placeOfIssueID;

    @Column("transport_document_type_code")
    private TransportDocumentTypeCode transportDocumentTypeCode;
}
