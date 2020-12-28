package org.dcsa.ebl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dcsa.core.model.GetId;
import org.dcsa.ebl.model.base.AbstractDocumentParty;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("document_party")
@Data
@EqualsAndHashCode(callSuper = true)
public class DocumentParty extends AbstractDocumentParty implements GetId<UUID> {

    @Id
    @JsonIgnore
    private UUID id;  /* TODO: Remove */

    @Column("shipping_instruction_id")
    private UUID shippingInstructionID;

    @Column("shipment_id")
    private UUID shipmentID;
}
