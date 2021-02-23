package org.dcsa.ebl.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dcsa.core.model.GetId;
import org.dcsa.ebl.model.transferobjects.ModelReferencingTO;
import org.dcsa.ebl.model.transferobjects.SetId;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.Size;
import java.util.UUID;

@Table("party")
@Data
@NoArgsConstructor
public class Party implements ModelReferencingTO<Party, UUID>, SetId<UUID>, GetId<UUID> {

    @Id
    @JsonProperty("partyID")
    private UUID id;

    @Column("party_name")
    @Size(max = 100)
    private String partyName;

    @Column("tax_reference_1")
    @Size(max = 20)
    private String taxReference1;

    @Column("tax_reference_2")
    @Size(max = 20)
    private String taxReference2;

    @Column("public_key")
    @Size(max = 500)
    private String publicKey;

    @Column("street_name")
    @Size(max = 100)
    private String streetName;

    @Column("street_number")
    @Size(max = 50)
    private String streetNumber;

    @Size(max = 50)
    private String floor;

    @Column("postal_code")
    @Size(max = 10)
    private String postalCode;

    @Column("city_name")
    @Size(max = 65)
    private String cityName;

    @Column("state_region")
    @Size(max = 65)
    private String stateRegion;

    @Size(max = 75)
    private String country;

    @Column("nmfta_code")
    @Size(max = 4)
    private String nmftaCode;

    public boolean isSolelyReferenceToModel() {
        if (this.getId() != null) {
            Party p = new Party();
            p.setId(this.getId());
            return this.equals(p);
        }
        return false;
    }

    public boolean isEqualsToModel(Party other) {
        return equals(other);
    }
}
