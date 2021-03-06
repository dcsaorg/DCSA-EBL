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

    @Column("is_to_be_notified")
    private Boolean isToBeNotified;
}
