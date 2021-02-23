package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.exception.UpdateException;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.dcsa.ebl.model.DocumentParty;
import org.dcsa.ebl.model.Party;
import org.dcsa.ebl.model.enums.PartyFunction;
import org.dcsa.ebl.model.transferobjects.DocumentPartyTO;
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

    public Mono<Void> deleteObsoleteDocumentPartyInstances(Iterable<DocumentPartyTO> documentPartyTOs) {
        return Flux.fromIterable(documentPartyTOs)
                .flatMap(documentPartyTO -> {
                    Party party = documentPartyTO.getParty();
                    UUID partyId = party.getId();
                    PartyFunction partyFunction = documentPartyTO.getPartyFunction();
                    if (partyId == null) {
                        return Mono.error(new AssertionError("Cannot delete a DocumentPartyTO without a" +
                                " partyID (on the Party member)"));
                    }
                    return Mono.zip(
                            Mono.just(partyId),
                            Mono.just(partyFunction),
                            deleteByPartyIDAndPartyFunctionAndShipmentID(partyId, partyFunction, null)
                    );
                }).flatMap(tuple -> {
                    UUID partyID = tuple.getT1();
                    PartyFunction partyFunction = tuple.getT2();
                    int deletion = tuple.getT3();
                    switch (deletion) {
                        case 1:
                            // Deleted as expected; nothing more to do.
                            return Mono.empty();
                        case 0:
                            // No deletion, this is probably because there is a shipmentID as well.
                            // TODO: The implementation of this is based on an assumption of how this case should be handled.
                            // (The alternatively being deleting the DocumentParty even though it references a Shipment.
                            return findByPartyIDAndPartyFunction(partyID, partyFunction)
                                    .doOnNext(documentParty -> documentParty.setShippingInstructionID(null))
                                    // Not the most efficient method, but will do for now.
                                    .flatMap(this::update);
                        default:
                            return Mono.error(new AssertionError("Deleted " + deletion + " rows but expected it to be 0 or 1!?"));
                    }
                })
                .then();
    }
}
