package org.dcsa.ebl.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode
public class TransportPlan {
    private List<ShipmentLocationTO> shipmentLocations;
    private List<Transport> transports;
}
