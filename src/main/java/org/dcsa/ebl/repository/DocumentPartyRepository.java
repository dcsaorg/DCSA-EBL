package org.dcsa.ebl.repository;

import org.dcsa.core.repository.ExtendedRepository;
import org.dcsa.ebl.model.DocumentParty;
import org.dcsa.ebl.model.enums.PartyFunction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface DocumentPartyRepository extends ExtendedRepository<DocumentParty, UUID> {
    Flux<DocumentParty> findAllByShippingInstructionID(UUID shippingInstructionID);
    Mono<DocumentParty> findByPartyIDAndPartyFunction(UUID partyID, PartyFunction partyFunction);
}
