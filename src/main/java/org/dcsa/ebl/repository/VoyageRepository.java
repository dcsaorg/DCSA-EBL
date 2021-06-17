package org.dcsa.ebl.repository;

import org.dcsa.core.repository.ExtendedRepository;
import org.dcsa.ebl.model.Voyage;
import org.springframework.data.r2dbc.repository.Query;
import reactor.core.publisher.Mono;

public interface VoyageRepository extends ExtendedRepository<Voyage, String> {

    @Query("SELECT voyage.* " +
            "FROM voyage JOIN transport_call_voyage on voyage.id = transport_call_voyage.voyage_id " +
            "WHERE transport_call_id = :transportCallID " +
            "ORDER BY carrier_voyage_number DESC " +
            "LIMIT 1")
    Mono<Voyage> findFirstByTransportCallOrderByCarrierVoyageNumberDesc(String transportCallID);
}
