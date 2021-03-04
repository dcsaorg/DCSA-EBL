package org.dcsa.ebl.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dcsa.ebl.model.enums.DCSATransportType;

@Data
@EqualsAndHashCode
public class Transport {
    private Location loadLocation;
    private Location dischargeLocation;
    private DCSATransportType modeOfTransport;
    private String vesselIMONumber;
    private String carrierVoyageNumber;
    private boolean isUnderShippersResponsibility;
}
