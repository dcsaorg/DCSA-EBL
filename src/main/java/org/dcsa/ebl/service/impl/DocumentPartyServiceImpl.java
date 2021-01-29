package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.exception.UpdateException;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.dcsa.ebl.model.DocumentParty;
import org.dcsa.ebl.model.enums.PartyFunction;
import org.dcsa.ebl.repository.DocumentPartyRepository;
import org.dcsa.ebl.service.DocumentPartyService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class DocumentPartyServiceImpl extends ExtendedBaseServiceImpl<DocumentPartyRepository, DocumentParty, UUID> implements DocumentPartyService {
    private final DocumentPartyRepository documentPartyRepository;

    @Override
    public DocumentPartyRepository getRepository() {
        return documentPartyRepository;
    }

    @Override
    public Class<DocumentParty> getModelClass() {
        return DocumentParty.class;
    }

    @Override
    public Flux<DocumentParty> findAllByShippingInstructionID(UUID shippingInstructionID) {
        return documentPartyRepository.findAllByShippingInstructionID(shippingInstructionID);
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
        return documentPartyRepository.findByPartyIDAndPartyFunction(update.getPartyID(), update.getPartyFunction())
                .flatMap(current -> this.preUpdateHook(current, update))
                .flatMap(this::save);
    }

    public Mono<DocumentParty> findByPartyIDAndPartyFunction(UUID partyID, PartyFunction partyFunction) {
        return documentPartyRepository.findByPartyIDAndPartyFunction(partyID, partyFunction);
    }

    public Mono<Integer> deleteByPartyIDAndPartyFunctionAndShipmentID(UUID partyID, PartyFunction partyFunction, UUID shipmentID) {
        return documentPartyRepository.deleteByPartyIDAndPartyFunctionAndShipmentID(partyID, partyFunction, shipmentID);
    }
}
