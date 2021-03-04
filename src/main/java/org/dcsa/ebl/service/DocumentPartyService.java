package org.dcsa.ebl.service;

import org.dcsa.core.service.ExtendedBaseService;
import org.dcsa.ebl.model.DocumentParty;
import org.dcsa.ebl.model.transferobjects.DocumentPartyTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface DocumentPartyService extends ExtendedBaseService<DocumentParty, UUID> {
    Flux<DocumentParty> findAllByShippingInstructionID(UUID shippingInstructionID);
    Mono<Void> deleteObsoleteDocumentPartyInstances(UUID shippingInstructionID);
    Flux<DocumentPartyTO> ensureResolvable(UUID shippingInstructionID, Iterable<DocumentPartyTO> documentPartyTOs);
}
