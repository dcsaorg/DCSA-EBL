package org.dcsa.ebl.service;

import org.dcsa.core.events.model.Charge;
import org.dcsa.core.service.ExtendedBaseService;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

public interface ChargeService extends ExtendedBaseService<Charge, String> {
    Flux<Charge> createAll(List<Charge> charges);
    Flux<Charge> findAllByTransportDocumentReference(String transportDocumentReference);
}
