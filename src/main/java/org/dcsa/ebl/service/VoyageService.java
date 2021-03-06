package org.dcsa.ebl.service;

import org.dcsa.core.service.ExtendedBaseService;
import org.dcsa.ebl.model.Voyage;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface VoyageService extends ExtendedBaseService<Voyage, String> {
    Mono<Voyage> findFirstByTransportCallOrderByCarrierVoyageNumberDesc(String transportCallID);
}
