package org.dcsa.ebl.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dcsa.core.model.AuditBase;
import org.dcsa.core.model.GetId;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Table("shipping_instruction")
@Data
@NoArgsConstructor
public class ShippingInstruction extends AuditBase implements GetId<String> {
    @JsonProperty("shippingInstructionID")
    private UUID id;

    @NotNull
    @Column("shipment_id")
    private UUID shipmentID;

    @NotNull
    @Column("callback_url")
    private String callbackUrl;
}
