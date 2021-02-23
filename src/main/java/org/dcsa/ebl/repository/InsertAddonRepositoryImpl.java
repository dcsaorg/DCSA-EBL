package org.dcsa.ebl.repository;

import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import reactor.core.publisher.Mono;

public class InsertAddonRepositoryImpl<T> implements InsertAddonRepository<T> {

    private final R2dbcEntityOperations entityOperations;

    public InsertAddonRepositoryImpl(R2dbcEntityOperations entityOperations) {
        this.entityOperations = entityOperations;
    }

    @Override
    public Mono<T> insert(T t) {
        return entityOperations.insert(t);
    }
}
