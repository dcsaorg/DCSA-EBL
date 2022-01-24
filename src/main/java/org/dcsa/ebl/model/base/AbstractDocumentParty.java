package org.dcsa.ebl.model.base;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dcsa.core.events.model.enums.PartyFunction;
import org.dcsa.core.model.AuditBase;
import org.springframework.data.relational.core.mapping.Column;

import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class AbstractDocumentParty extends AuditBase {

    @Column("party_function")
    @NotNull
    private PartyFunction partyFunction;

    @Column("is_to_be_notified")
    private Boolean isToBeNotified;
}
