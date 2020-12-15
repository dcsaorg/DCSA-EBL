package org.dcsa.ebl.repository;

import org.dcsa.core.repository.ExtendedRepository;
import org.dcsa.ebl.model.DocumentParty;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface DocumentPartyRepository extends ExtendedRepository<DocumentParty, UUID> {
    Flux<DocumentParty> findAllByShippingInstructionID(UUID shippingInstructionID);
}
