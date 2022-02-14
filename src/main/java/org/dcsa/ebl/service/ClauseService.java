package org.dcsa.ebl.service;

import org.dcsa.ebl.model.Clause;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface ClauseService {
    Flux<Clause> createAll(List<Clause> clauses);

    // Misuse this class to create the Many-Many relation between TransportDocument and Clause
    // as Spring R2DBC does not support combined keys in ModelClasses
    Mono<Void> createTransportDocumentClauseRelation(UUID clauseID, String transportDocumentReference);
}
