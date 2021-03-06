package org.dcsa.ebl.model.transferobjects;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dcsa.ebl.model.Party;
import org.dcsa.ebl.model.PartyContactDetails;
import org.dcsa.ebl.model.base.AbstractDocumentParty;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class DocumentPartyTO extends AbstractDocumentParty {

    @NotNull
    private PartyTO party;

    private PartyContactDetails partyContactDetails;

    private List<String> displayedAddress;
}
