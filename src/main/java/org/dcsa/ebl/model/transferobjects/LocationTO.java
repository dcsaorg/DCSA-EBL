package org.dcsa.ebl.model.transferobjects;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.dcsa.ebl.Util;
import org.dcsa.ebl.model.Address;
import org.dcsa.ebl.model.Location;
import org.dcsa.ebl.model.base.AbstractLocation;
import org.dcsa.ebl.model.utils.MappingUtil;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class LocationTO extends AbstractLocation implements ModelReferencingTO<Location, UUID>, SetId<UUID> {

    private Address address;

    @Override
    public boolean isSolelyReferenceToModel() {
        return Util.containsOnlyID(this, LocationTO::new);
    }

    public boolean isEqualsToModel(Location other) {
        return this.toLocation().equals(other);
    }

    public Location toLocation() {
        Location location = MappingUtil.instanceFrom(this, Location::new, AbstractLocation.class);
        if (this.address != null) {
            location.setAddressID(this.address.getId());
        }
        return location;
    }
}
