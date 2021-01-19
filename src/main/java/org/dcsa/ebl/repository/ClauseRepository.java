package org.dcsa.ebl.repository;

import org.dcsa.core.repository.ExtendedRepository;
import org.dcsa.ebl.model.Clause;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ClauseRepository extends ExtendedRepository<Clause, UUID> {
    @Query("SELECT id, clause_content from carrier_clauses " +
           "JOIN transport_document_carrier_clauses ON id = carrier_clause_id " +
           "WHERE transport_document_id = :transportDocumentID")
    Flux<Clause> findAllByTransportDocumentID(UUID transportDocumentID);

    @Modifying
    @Query("INSERT INTO transport_document_carrier_clauses (" +
           "carrier_clause_id, transport_document_id" +
           ") VALUES (" +
           ":clauseID, :transportDocumentID" +
           ")")
    Mono<Void> createTransportDocumentIDClauseIDRelation(UUID clauseID, UUID transportDocumentID);
}
