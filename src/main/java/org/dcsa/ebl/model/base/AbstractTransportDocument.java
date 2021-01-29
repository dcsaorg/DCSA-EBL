package org.dcsa.ebl.model.base;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.dcsa.core.model.AuditBase;
import org.dcsa.core.model.GetId;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public abstract class AbstractTransportDocument extends AuditBase implements GetId<UUID> {

    @Id
    @JsonProperty("transportDocumentID")
    private UUID id;

    @Column("date_of_issue")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateOfIssue;

    @Column("onboard_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate onboardDate;

    @Column("received_for_shipment_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate receivedForShipmentDate;

    @Column("document_reference_number")
    @Size(max = 20)
    private String documentReferenceNumber;

    @Column("terms_and_conditions")
    private String termsAndConditions;

    @Column("issuer")
    @Size(max = 4)
    private String issuer;

    @Column("shipping_instruction_id")
    private UUID shippingInstructionID;

    @Column("declared_value_currency")
    @Size(max = 3)
    private String declaredValueCurrency;

    @Column("declared_value")
    private Float declaredValue;

    @Column("number_of_rider_pages")
    private Integer numberOfRiderPages;
}
