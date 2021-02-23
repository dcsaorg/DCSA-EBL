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

@Table("location")
@Data
@NoArgsConstructor
public class Location implements ModelReferencingTO<Location, UUID>, SetId<UUID>, GetId<UUID> {
    @Id
    @JsonProperty("locationID")
    private UUID id;

    @Size(max = 100)
    private String locationName;

    @Size(max = 250)
    private String address;

    @Size(max = 10)
    private String latitude;

    @Size(max = 11)
    private String longitude;

    @JsonProperty("UNLocationCode")
    @Column("un_location_code")
    @Size(max = 5)
    private String unLocationCode;

    public boolean isSolelyReferenceToModel() {
        if (this.getId() != null) {
            Location location = new Location();
            location.setId(this.getId());
            return this.equals(location);
        }
        return false;
    }

    public boolean isEqualsToModel(Location other) {
        return equals(other);
    }
}
