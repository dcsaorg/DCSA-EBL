package org.dcsa.ebl.service;

import org.dcsa.core.service.ExtendedBaseService;
import org.dcsa.ebl.model.Location;
import org.dcsa.ebl.model.transferobjects.LocationTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface LocationService extends ExtendedBaseService<Location, UUID> {

    Mono<LocationTO> ensureResolvable(LocationTO locationTO);

    Mono<LocationTO> findPaymentLocationByShippingInstructionID(UUID shippingInstructionID);
}
