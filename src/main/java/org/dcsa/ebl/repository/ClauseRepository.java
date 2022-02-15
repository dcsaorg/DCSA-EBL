package org.dcsa.ebl.repository;

import org.dcsa.ebl.model.Clause;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ClauseRepository extends ReactiveCrudRepository<Clause, UUID> {

    @Modifying
    @Query("INSERT INTO transport_document_carrier_clauses (" +
           "carrier_clause_id, transport_document_reference" +
           ") VALUES (" +
           ":clauseID, :transportDocumentReference" +
           ")")
    Mono<Void> createTransportDocumentReferenceClauseIDRelation(UUID clauseID, String transportDocumentReference);
}
