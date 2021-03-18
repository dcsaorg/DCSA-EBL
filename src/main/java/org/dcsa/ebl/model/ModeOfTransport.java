package org.dcsa.ebl.model;

import lombok.Data;
import org.dcsa.ebl.model.enums.DCSATransportType;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import javax.persistence.Id;
import javax.validation.constraints.Size;

@Table("mode_of_transport")
@Data
public class ModeOfTransport {
    @Id
    @Column("mode_of_transport_code")
    @Size(max = 3)
    private String modeOfTransportCode;

    @Column("mode_of_transport_name")
    @Size(max = 100)
    private String modeOfTransportName;

    @Column("mode_of_transport_description")
    @Size(max = 250)
    private String modeOfTransportDescription;

    @Column("dcsa_transport_type")
    @Size(max = 50)
    private DCSATransportType modeOfTransportType;
}
