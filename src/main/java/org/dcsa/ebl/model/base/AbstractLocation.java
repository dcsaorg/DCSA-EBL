package org.dcsa.ebl.model.base;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dcsa.core.model.GetId;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

import javax.validation.constraints.Size;
import java.util.UUID;

@Data
@NoArgsConstructor
public class AbstractLocation implements GetId<UUID> {
    @Id
    @JsonProperty("locationID")
    private UUID id;

    @Size(max = 100)
    private String locationName;

    @Size(max = 10)
    private String latitude;

    @Size(max = 11)
    private String longitude;

    @JsonProperty("UNLocationCode")
    @Column("un_location_code")
    @Size(max = 5)
    private String unLocationCode;

}
