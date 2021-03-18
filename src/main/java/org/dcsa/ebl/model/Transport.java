package org.dcsa.ebl.model.base;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dcsa.core.model.GetId;
import org.dcsa.ebl.model.enums.DCSATransportType;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

import javax.validation.constraints.Size;
import java.util.UUID;

@Data
@NoArgsConstructor
public class AbstractTransport implements GetId<UUID> {
    @Id
    private UUID id;

    @Column("transport_reference")
    @Size(max = 50)
    private String transportReference;

    @Column("transport_name")
    @Size(max = 100)
    private String transportName;

    @Column("transport_name")
    private DCSATransportType modeOfTransport;

    @Size(max = 11)
    private String longitude;

    @JsonProperty("UNLocationCode")
    @Column("un_location_code")
    @Size(max = 5)
    private String unLocationCode;

    mode_of_transport varchar(3) NULL,
    load_transport_call_id uuid NOT NULL,
    discharge_transport_call_id uuid NOT NULL,
    vessel varchar(7) NULL
}
