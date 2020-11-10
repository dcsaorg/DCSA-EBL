package org.dcsa.ebl.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dcsa.core.model.GetId;
import org.dcsa.ebl.model.enums.DocumentStatus;
import org.dcsa.ebl.model.enums.WeightUnit;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

@Table("transport_document")
@Data
@NoArgsConstructor
public class TransportDocument extends BaseClass implements GetId<UUID> {

    @Id
    @JsonProperty("transportDocumentID")
    private UUID id;

    @Column("transport_document_type_code")
    @Size(max = 3)
    private String transportDocumentType;

    @Column("onboard_date")
    private LocalDate onboardDate;

    @Column("received_for_shipment_date")
    private LocalDate receivedForShipmentDate;

    @Column("document_reference_number")
    @Size(max = 20)
    private String documentReferenceNumber;

    @Column("number_of_originals")
    private Integer numberOfOriginals;

    @Column("issuer")
    private UUID issuer;

    @Column("document_status")
    @Size(max = 50)
    private DocumentStatus documentStatus;

    public void setDocumentStatus(String documentStatus) {
        this.documentStatus = DocumentStatus.valueOf(documentStatus);
    }

    @Column("shipping_instruction")
    private UUID shippingInstruction;

    @Column("declared_value")
    private Float declaredValue;

    @Column("declared_value_currency")
    @Size(max = 3)
    private String declaredValueCurrency;
}
