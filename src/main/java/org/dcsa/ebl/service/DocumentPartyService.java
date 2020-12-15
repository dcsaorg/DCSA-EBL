package org.dcsa.ebl.service;

import org.dcsa.core.service.ExtendedBaseService;
import org.dcsa.ebl.model.DocumentParty;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface DocumentPartyService extends ExtendedBaseService<DocumentParty, UUID> {
    Flux<DocumentParty> findAllByShippingInstructionID(UUID shippingInstructionID);
}
