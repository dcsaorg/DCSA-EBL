package org.dcsa.ebl.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dcsa.core.model.GetId;
import org.dcsa.ebl.Util;
import org.dcsa.ebl.model.transferobjects.ModelReferencingTO;
import org.dcsa.ebl.model.transferobjects.SetId;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.Size;
import java.util.UUID;

@Table("address")
@Data
@NoArgsConstructor
public class Address implements ModelReferencingTO<Address, UUID>, SetId<UUID> {

    @Id
    @JsonProperty("id")
    private UUID id;

    @Size(max = 100)
    private String name;

    @Size(max = 100)
    private String street;

    @Size(max = 50)
    @Column("street_number")
    private String streetNumber;

    @Size(max = 50)
    private String floor;

    @Column("postal_code")
    @Size(max = 10)
    private String postalCode;

    @Size(max = 65)
    private String city;

    @Column("state_region")
    @Size(max = 75)
    private String stateRegion;

    @Size(max = 75)
    private String country;

    @Override
    public boolean isSolelyReferenceToModel() {
        return Util.containsOnlyID(this, Address::new);
    }

    @Override
    public boolean isEqualsToModel(Address address) {
        return equals(address);
    }
}
