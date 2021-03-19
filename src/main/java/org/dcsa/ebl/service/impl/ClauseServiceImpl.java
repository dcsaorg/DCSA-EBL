package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.dcsa.ebl.Util;
import org.dcsa.ebl.model.Clause;
import org.dcsa.ebl.repository.ClauseRepository;
import org.dcsa.ebl.service.ClauseService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ClauseServiceImpl extends ExtendedBaseServiceImpl<ClauseRepository, Clause, UUID> implements ClauseService {
    private final ClauseRepository clauseRepository;

    @Override
    public ClauseRepository getRepository() {
        return clauseRepository;
    }

    @Override
    public Flux<Clause> createAll(List<Clause> Clauses) {
        return Flux.fromIterable(Clauses)
                .concatMap(this::preCreateHook)
                .concatMap(this::preSaveHook)
                .buffer(Util.SQL_LIST_BUFFER_SIZE)
                .concatMap(clauseRepository::saveAll);
    }

    @Override
    public Flux<Clause> findAllByTransportDocumentID(UUID transportDocumentID) {
        return clauseRepository.findAllByTransportDocumentID(transportDocumentID);
    }

    // Misuse this class to create the Many-Many relation between TransportDocument and Clause
    // as Spring R2DBC does not support combined keys in ModelClasses
    @Override
    public Mono<Void> createTransportDocumentClauseRelation(UUID clauseID, UUID transportDocumentID) {
        return clauseRepository.createTransportDocumentIDClauseIDRelation(clauseID, transportDocumentID);
    }
}
