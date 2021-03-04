package org.dcsa.ebl.service;

import org.dcsa.core.service.ExtendedBaseService;
import org.dcsa.ebl.model.Address;
import org.dcsa.ebl.model.transferobjects.LocationTO;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface AddressService extends ExtendedBaseService<Address, UUID> {
    Mono<Address> ensureResolvable(Address address);
}
