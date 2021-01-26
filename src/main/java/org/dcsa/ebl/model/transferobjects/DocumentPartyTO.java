package org.dcsa.ebl.model.transferobjects;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dcsa.ebl.model.Party;
import org.dcsa.ebl.model.base.AbstractDocumentParty;

import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class DocumentPartyTO extends AbstractDocumentParty {

    @NotNull
    private Party party;
}
