package org.dcsa.ebl.repository;

import org.dcsa.core.repository.ExtendedRepository;
import org.dcsa.ebl.model.Reference;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface ReferenceRepository extends ExtendedRepository<Reference, UUID> {

    Flux<Reference> findAllByShippingInstructionID(String shippingInstructionID);

}
