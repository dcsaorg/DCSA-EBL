package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.events.model.Charge;
import org.dcsa.core.events.repository.ChargeRepository;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.dcsa.core.util.MappingUtils;
import org.dcsa.ebl.service.ChargeService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ChargeServiceImpl extends ExtendedBaseServiceImpl<ChargeRepository, Charge, String> implements ChargeService {
    private final ChargeRepository chargeRepository;

    @Override
    public ChargeRepository getRepository() {
        return chargeRepository;
    }

    @Override
    public Flux<Charge> createAll(List<Charge> charges) {
        return Flux.fromIterable(charges)
                .concatMap(this::preCreateHook)
                .concatMap(this::preSaveHook)
                .buffer(MappingUtils.SQL_LIST_BUFFER_SIZE)
                .concatMap(chargeRepository::saveAll);
    }

    @Override
    public Flux<Charge> findAllByTransportDocumentReference(String transportDocumentReference) {
        return Flux.empty();
    }
}
