package org.dcsa.ebl.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.dcsa.core.model.GetId;

import javax.validation.constraints.Size;
import java.util.UUID;

@Data
@NoArgsConstructor
public class Location implements GetId<UUID> {
    private UUID id;

    @Size(max = 250)
    private String address;

    @Size(max = 10)
    private String latitude;

    @Size(max = 11)
    private String longitude;

    @Size(max = 5)
    private String un_location_code;
}
