package org.dcsa.ebl.service;

import org.dcsa.core.service.ExtendedBaseService;
import org.dcsa.ebl.model.Clause;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface ClauseService extends ExtendedBaseService<Clause, UUID> {
    Flux<Clause> createAll(List<Clause> clauses);
    Flux<Clause> findAllByTransportDocumentID(String transportDocumentReference);

    // Misuse this class to create the Many-Many relation between TransportDocument and Clause
    // as Spring R2DBC does not support combined keys in ModelClasses
    Mono<Void> createTransportDocumentClauseRelation(UUID clauseID, String transportDocumentReference);
}
