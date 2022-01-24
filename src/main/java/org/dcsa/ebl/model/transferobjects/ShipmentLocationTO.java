package org.dcsa.ebl.model.transferobjects;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dcsa.core.events.model.transferobjects.LocationTO;
import org.dcsa.ebl.model.base.AbstractShipmentLocation;

@Data
@EqualsAndHashCode(callSuper = true)
public class ShipmentLocationTO extends AbstractShipmentLocation {

    private LocationTO location;
}
