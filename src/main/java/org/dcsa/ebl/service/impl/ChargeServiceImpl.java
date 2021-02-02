package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.dcsa.ebl.Util;
import org.dcsa.ebl.model.Charge;
import org.dcsa.ebl.repository.ChargeRepository;
import org.dcsa.ebl.service.ChargeService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ChargeServiceImpl extends ExtendedBaseServiceImpl<ChargeRepository, Charge, UUID> implements ChargeService {
    private final ChargeRepository chargeRepository;

    @Override
    public ChargeRepository getRepository() {
        return chargeRepository;
    }

    @Override
    public Class<Charge> getModelClass() {
        return Charge.class;
    }

    @Override
    public Flux<Charge> createAll(List<Charge> charges) {
        return Flux.fromIterable(charges)
                .concatMap(this::preCreateHook)
                .concatMap(this::preSaveHook)
                .buffer(Util.SQL_LIST_BUFFER_SIZE)
                .concatMap(chargeRepository::saveAll);
    }

    @Override
    public Flux<Charge> findAllByTransportDocumentID(UUID transportDocumentID) {
        return chargeRepository.findAllByTransportDocumentID(transportDocumentID);
    }
}
