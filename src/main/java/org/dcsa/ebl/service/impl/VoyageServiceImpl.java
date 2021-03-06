package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.dcsa.ebl.model.Voyage;
import org.dcsa.ebl.repository.VoyageRepository;
import org.dcsa.ebl.service.VoyageService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class VoyageServiceImpl extends ExtendedBaseServiceImpl<VoyageRepository, Voyage, String> implements VoyageService {
    private final VoyageRepository voyageRepository;

    @Override
    public VoyageRepository getRepository() {
        return voyageRepository;
    }

    @Override
    public Mono<Voyage> findFirstByTransportCallOrderByCarrierVoyageNumberDesc(String transportCallID) {
        return getRepository().findFirstByTransportCallOrderByCarrierVoyageNumberDesc(transportCallID);
    }
}
