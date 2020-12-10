package org.dcsa.ebl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.dcsa.core.model.AuditBase;
import org.dcsa.core.model.GetId;
import org.dcsa.ebl.model.base.AbstractDocumentParty;
import org.dcsa.ebl.model.enums.PartyFunction;
import org.springframework.data.relational.core.mapping.Column;

import javax.persistence.Id;
import javax.validation.constraints.Size;
import java.util.UUID;

@Data
public class DocumentParty extends AbstractDocumentParty implements GetId<UUID> {

    @Id
    @JsonIgnore
    private UUID id;  /* TODO: Remove */

}
