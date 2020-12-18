package org.dcsa.ebl.model.transferobjects;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dcsa.ebl.model.Party;
import org.dcsa.ebl.model.base.AbstractDocumentParty;

@Data
@EqualsAndHashCode(callSuper = true)
public class DocumentPartyTO extends AbstractDocumentParty {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Party party;
}
