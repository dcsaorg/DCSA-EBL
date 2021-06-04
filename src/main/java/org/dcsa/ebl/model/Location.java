package org.dcsa.ebl.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.dcsa.ebl.model.base.AbstractLocation;
import org.dcsa.ebl.model.transferobjects.LocationTO;
import org.dcsa.ebl.model.utils.MappingUtil;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;
import java.util.UUID;

@Table("location")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Location extends AbstractLocation {

    @Column("address_id")
    private UUID addressID;

    public LocationTO toLocationTO(Address address) {
        LocationTO locationTO;
        UUID providedAddressID = address != null ? address.getId() : null;
        if (!Objects.equals(addressID, providedAddressID)) {
            throw new IllegalArgumentException("address does not match addressID");
        }
        locationTO = MappingUtil.instanceFrom(this, LocationTO::new, AbstractLocation.class);
        locationTO.setAddress(address);
        return locationTO;
    }
}
