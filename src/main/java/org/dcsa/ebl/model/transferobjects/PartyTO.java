package org.dcsa.ebl.model.transferobjects;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.dcsa.core.model.GetId;
import org.dcsa.ebl.Util;
import org.dcsa.ebl.model.Address;
import org.dcsa.ebl.model.Party;
import org.dcsa.ebl.model.base.AbstractParty;
import org.dcsa.ebl.model.utils.MappingUtil;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PartyTO extends AbstractParty implements ModelReferencingTO<Party, UUID>, SetId<UUID> {

    private Address address;

    private UUID addressID;

    @Override
    public boolean isSolelyReferenceToModel() {
        return Util.containsOnlyID(this, PartyTO::new);
    }

    public boolean isEqualsToModel(Party other) {
        return this.toParty().equals(other);
    }

    public Party toParty() {
        Party party = MappingUtil.instanceFrom(this, Party::new, AbstractParty.class);
        if (this.address != null) {
            party.setAddressID(address.getId());
        }
        return party;
    }
}