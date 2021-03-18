package org.dcsa.ebl.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.dcsa.core.model.GetId;
import org.dcsa.ebl.model.enums.DCSATransportType;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.Size;
import java.util.UUID;

@Data
@NoArgsConstructor
@Table("transport")
public class Transport implements GetId<UUID> {
    @Id
    private UUID id;

    @Column("transport_reference")
    @Size(max = 50)
    private String transportReference;

    @Column("transport_name")
    @Size(max = 100)
    private String transportName;

    @Column("mode_of_transport")
    private DCSATransportType modeOfTransport;

    @Column("load_transport_call_id")
    private UUID loadTransportCall;

    @Column("discharge_transport_call_id")
    private UUID dischargeTransportCall;

    @Column("vessel")
    @Size(max = 7)
    private String vesselIMONumber;
}
