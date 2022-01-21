package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.events.model.DocumentParty;
import org.dcsa.core.events.repository.DocumentPartyRepository;
import org.dcsa.core.events.service.PartyService;
import org.dcsa.core.exception.UpdateException;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.dcsa.ebl.model.transferobjects.DocumentPartyTO;
import org.dcsa.ebl.service.DisplayedAddressService;
import org.dcsa.ebl.service.DocumentPartyService;
import org.dcsa.ebl.service.PartyContactDetailsService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class DocumentPartyServiceImpl extends ExtendedBaseServiceImpl<DocumentPartyRepository, DocumentParty, UUID> implements DocumentPartyService {
    private final DocumentPartyRepository documentPartyRepository;
    private final PartyContactDetailsService partyContactDetailsService;
    private final PartyService partyService;
    private final DisplayedAddressService displayedAddressService;

    @Override
    public DocumentPartyRepository getRepository() {
        return documentPartyRepository;
    }

    @Override
    public Flux<DocumentParty> findAllByShippingInstructionID(String shippingInstructionID) {
        return Flux.empty();
    }

    @Override
    public Mono<DocumentParty> findById(UUID id) {
        return Mono.error(new UnsupportedOperationException("findById not supported"));
    }

    protected Mono<DocumentParty> preUpdateHook(DocumentParty current, DocumentParty update) {
        // FIXME: Revise this when we get compound Id support figured out
        if (!current.getPartyID().equals(update.getPartyID())) {
            return Mono.error(new UpdateException("update called with a non-matching item!"));
        }
        if (!current.getPartyFunction().equals(update.getPartyFunction())) {
            return Mono.error(new UpdateException("update called with a non-matching item!"));
        }
        update.setId(current.getId());
        return super.preUpdateHook(current, update);
    }

    @Override
    public Mono<DocumentParty> update(final DocumentParty update) {
        return Mono.empty();
    }

    public Mono<Void> deleteObsoleteDocumentPartyInstances(String shippingInstructionID) {
        return Mono.empty();
    }

    @Override
    public Flux<DocumentPartyTO> ensureResolvable(String shippingInstructionID, Iterable<DocumentPartyTO> documentPartyTOs) {
        return Flux.empty();
    }

}
