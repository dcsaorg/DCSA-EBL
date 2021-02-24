package org.dcsa.ebl.service;

import org.dcsa.core.service.ExtendedBaseService;
import org.dcsa.ebl.model.Party;
import org.dcsa.ebl.model.transferobjects.PartyTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PartyService extends ExtendedBaseService<Party, UUID> {
    Flux<Party> findAllById(Iterable<UUID> ids);
    Mono<PartyTO> ensureResolvable(PartyTO partyTO);
}
