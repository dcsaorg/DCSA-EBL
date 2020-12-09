package org.dcsa.ebl.service;

import org.dcsa.core.service.ExtendedBaseService;
import org.dcsa.ebl.model.Reference;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface ReferenceService extends ExtendedBaseService<Reference, UUID> {

    Flux<Reference> findAllByShippingInstructionID(UUID shippingInstructionID);
}
