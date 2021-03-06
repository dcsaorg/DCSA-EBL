package org.dcsa.ebl.service;

import org.dcsa.core.service.ExtendedBaseService;
import org.dcsa.ebl.model.Charge;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

public interface ChargeService extends ExtendedBaseService<Charge, UUID> {
    Flux<Charge> createAll(List<Charge> charges);
    Flux<Charge> findAllByTransportDocumentReference(String transportDocumentReference);
}
