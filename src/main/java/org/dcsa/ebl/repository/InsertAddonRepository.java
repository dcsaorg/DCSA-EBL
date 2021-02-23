package org.dcsa.ebl.repository;

import reactor.core.publisher.Mono;

public interface InsertAddonRepository<T> {

    /**
     * Unsafe create that enables you to insert an entity with a predefined
     * ID.
     */
    Mono<T> insert(T t);
}
