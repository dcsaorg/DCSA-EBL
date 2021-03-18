package org.dcsa.ebl.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dcsa.ebl.model.transferobjects.ShipmentLocationTO;
import org.dcsa.ebl.model.transferobjects.TransportTO;

import java.util.List;

@Data
@EqualsAndHashCode
public class TransportPlan {
    private List<ShipmentLocationTO> shipmentLocations;
    private List<TransportTO> transports;
}
