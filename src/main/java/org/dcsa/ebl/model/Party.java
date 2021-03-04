package org.dcsa.ebl.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.dcsa.core.model.GetId;
import org.dcsa.ebl.model.base.AbstractParty;
import org.dcsa.ebl.model.transferobjects.PartyTO;
import org.dcsa.ebl.model.transferobjects.SetId;
import org.dcsa.ebl.model.utils.MappingUtil;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;
import java.util.UUID;

@Table("party")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Party extends AbstractParty implements SetId<UUID> {

    @Column("address_id")
    private UUID addressID;

    public PartyTO toPartyTO(Address address) {
        PartyTO partyTO = MappingUtil.instanceFrom(this, PartyTO::new, AbstractParty.class);
        UUID providedAddressID = address != null ? address.getId() : null;
        if (!Objects.equals(addressID, providedAddressID)) {
            throw new IllegalArgumentException("address does not match addressID");
        }
        partyTO.setAddress(address);
        return partyTO;
    }
}
