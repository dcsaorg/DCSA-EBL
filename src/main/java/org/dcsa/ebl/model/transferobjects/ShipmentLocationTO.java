package org.dcsa.ebl.model.transferobjects;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dcsa.ebl.model.base.AbstractShipmentLocation;
import org.dcsa.ebl.model.transferobjects.LocationTO;

@Data
@EqualsAndHashCode(callSuper = true)
public class ShipmentLocationTO extends AbstractShipmentLocation {

    private LocationTO location;
}
