package org.dcsa.ebl.service;

import org.dcsa.core.service.ExtendedBaseService;
import org.dcsa.ebl.model.Location;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface LocationService extends ExtendedBaseService<Location, UUID> {

    Flux<Location> findAllById(Iterable<UUID> ids);

}
