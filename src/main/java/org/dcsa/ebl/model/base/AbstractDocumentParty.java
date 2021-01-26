package org.dcsa.ebl.model.base;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dcsa.core.model.AuditBase;
import org.dcsa.ebl.model.enums.PartyFunction;
import org.springframework.data.relational.core.mapping.Column;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
public class AbstractDocumentParty extends AuditBase {

    @Column("party_function")
    @NotNull
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
