package org.dcsa.ebl.model.transferobjects;

import lombok.Data;
import org.dcsa.ebl.model.Party;
import org.dcsa.ebl.model.base.AbstractDocumentParty;

@Data
public class DocumentPartyTO extends AbstractDocumentParty {
    private Party party;
}
