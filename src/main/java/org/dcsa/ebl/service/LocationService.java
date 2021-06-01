package org.dcsa.ebl.service;

import org.dcsa.core.service.ExtendedBaseService;
import org.dcsa.ebl.model.Location;
import org.dcsa.ebl.model.transferobjects.LocationTO;
import reactor.core.publisher.Mono;

public interface LocationService extends ExtendedBaseService<Location, String> {

    Mono<LocationTO> ensureResolvable(LocationTO locationTO);

    Mono<LocationTO> findPaymentLocationByShippingInstructionID(String shippingInstructionID);

    Mono<LocationTO> findTOById(String locationID);
}
