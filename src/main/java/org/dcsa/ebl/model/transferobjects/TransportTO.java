package org.dcsa.ebl.model.transferobjects;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dcsa.core.events.model.enums.DCSATransportType;
import org.dcsa.core.events.model.transferobjects.LocationTO;

@Data
@EqualsAndHashCode
public class TransportTO {
    private LocationTO loadLocation;
    private LocationTO dischargeLocation;
    private DCSATransportType modeOfTransport;
    private String vesselIMONumber;
    private String carrierVoyageNumber;
    private boolean isUnderShippersResponsibility;
}
