package org.dcsa.ebl.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.dcsa.core.model.AuditBase;
import org.dcsa.core.model.GetId;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.Size;
import java.util.UUID;

@Table("charges")
@NoArgsConstructor
@Data
public class Charge extends AuditBase implements GetId<UUID> {
    @Id
    private UUID id;

    @Column("transport_document_id")
    private UUID transportDocumentID;

    @Column("shipment_id")
    private UUID shipmentID;

    @Column("charge_type")
    @Size(max = 20)
    private String chargeType;

    @Column("currency_amount")
    private Float currencyAmount;

    @Column("currency_code")
    @Size(max = 3)
    private String currencyCode;

    @Column("payment_term")
    @Size(max = 3)
    private String paymentTerm;

    @Column("calculation_basis")
    @Size(max = 50)
    private String calculationBasis;

    @Column("freight_payable_at")
    private UUID freightPayableAt;

    @Column("is_charge_displayed")
    private Boolean isChargeDisplayed;

    @Column("unit_price")
    private Float unitPrice;

    private Float quantity;
}
