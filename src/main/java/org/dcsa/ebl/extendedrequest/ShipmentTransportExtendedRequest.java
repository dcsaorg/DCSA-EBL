package org.dcsa.ebl.extendedrequest;

import org.dcsa.core.extendedrequest.ExtendedParameters;
import org.dcsa.core.extendedrequest.ExtendedRequest;
import org.dcsa.ebl.model.ShipmentTransport;

public class ShipmentLocationExtendedRequest<T extends ShipmentTransport> extends ExtendedRequest<T> {
    public ShipmentLocationExtendedRequest(ExtendedParameters extendedParameters, Class<T> modelClass) {
        super(extendedParameters, modelClass);
    }
}
