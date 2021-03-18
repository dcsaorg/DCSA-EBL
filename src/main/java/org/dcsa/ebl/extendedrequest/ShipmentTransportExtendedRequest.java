package org.dcsa.ebl.extendedrequest;

import org.dcsa.core.extendedrequest.ExtendedParameters;
import org.dcsa.core.extendedrequest.ExtendedRequest;
import org.dcsa.ebl.model.combined.ExtendedShipmentTransport;

public class ShipmentTransportExtendedRequest extends ExtendedRequest<ExtendedShipmentTransport> {
    public ShipmentTransportExtendedRequest(ExtendedParameters extendedParameters, Class<ExtendedShipmentTransport> modelClass) {
        super(extendedParameters, modelClass);
    }
}
