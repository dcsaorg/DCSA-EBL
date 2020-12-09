package org.dcsa.ebl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.dcsa.core.model.AuditBase;
import org.dcsa.core.model.GetId;
import org.dcsa.ebl.model.enums.PartyFunction;
import org.springframework.data.relational.core.mapping.Column;

import javax.persistence.Id;
import javax.validation.constraints.Size;
import java.util.UUID;

@Data
public class DocumentParty extends AuditBase implements GetId<UUID> {

    @Id
    @JsonIgnore
    private UUID id;  /* TODO: Remove */

    @Column("shipping_instruction_id")
    private UUID shippingInstructionID;

    @Column("party_id")
    private UUID partyID;

    @Column("shipment_id")
    private UUID shipmentID;

    @Column("party_function")
    @Size(max = 3)
    private PartyFunction partyFunction;

    public void setPartyFunction(String partyFunction) {
        this.partyFunction = PartyFunction.valueOf(partyFunction);
    }

    public void setPartyFunction(PartyFunction partyFunction) {
        this.partyFunction = partyFunction;
    }

    @Column("displayed_address")
    @Size(max = 250)
    private String displayedAddress;

    @Column("party_contact_details")
    @Size(max = 250)
    private String partyContactDetails;

    @Column("should_be_notified")
    private Boolean shouldBeNotified;
}
