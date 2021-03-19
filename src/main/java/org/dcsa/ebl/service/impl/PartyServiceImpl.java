package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.dcsa.ebl.Util;
import org.dcsa.ebl.model.Address;
import org.dcsa.ebl.model.Party;
import org.dcsa.ebl.model.transferobjects.PartyTO;
import org.dcsa.ebl.repository.PartyRepository;
import org.dcsa.ebl.service.AddressService;
import org.dcsa.ebl.service.PartyService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class PartyServiceImpl extends ExtendedBaseServiceImpl<PartyRepository, Party, UUID> implements PartyService {
    private final AddressService addressService;
    private final PartyRepository partyRepository;

    @Override
    public PartyRepository getRepository() {
        return partyRepository;
    }

    public Flux<Party> findAllById(Iterable<UUID> ids) {
        return partyRepository.findAllById(ids);
    }

    @Override
    public Mono<PartyTO> ensureResolvable(PartyTO partyTO){
        Address address = partyTO.getAddress();
        Mono<PartyTO> partyTOMono;
        if (address != null) {
            partyTOMono = addressService.ensureResolvable(address)
                    .doOnNext(partyTO::setAddress)
                    .thenReturn(partyTO);
        } else {
            partyTOMono = Mono.just(partyTO);
        }

        return partyTOMono
                .flatMap(pTo -> Util.resolveModelReference(
                        pTo,
                        this::findById,
                        pTO -> this.create(pTO.toParty()),
                        "Party"
                )).map(party -> party.toPartyTO(partyTO.getAddress()));
    }
}
