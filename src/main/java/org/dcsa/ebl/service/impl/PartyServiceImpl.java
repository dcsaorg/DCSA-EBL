package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.dcsa.ebl.model.Party;
import org.dcsa.ebl.repository.PartyRepository;
import org.dcsa.ebl.service.PartyService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class PartyServiceImpl extends ExtendedBaseServiceImpl<PartyRepository, Party, UUID> implements PartyService {
    private final PartyRepository partyRepository;

    @Override
    public PartyRepository getRepository() {
        return partyRepository;
    }

    @Override
    public Class<Party> getModelClass() {
        return Party.class;
    }

    public Flux<Party> findAllById(Iterable<UUID> ids) {
        return partyRepository.findAllById(ids);
    }
}
