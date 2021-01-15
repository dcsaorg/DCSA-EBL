package org.dcsa.ebl.repository;

import org.dcsa.core.repository.ExtendedRepository;
import org.dcsa.ebl.model.Charge;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface ChargeRepository extends ExtendedRepository<Charge, UUID> {
    Flux<Charge> findAllByTransportDocumentID(UUID transportDocumentID);
}
