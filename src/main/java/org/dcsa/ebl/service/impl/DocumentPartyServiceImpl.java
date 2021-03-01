package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.exception.UpdateException;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.dcsa.ebl.Util;
import org.dcsa.ebl.model.DocumentParty;
import org.dcsa.ebl.model.utils.MappingUtil;
import org.dcsa.ebl.repository.DisplayedAddressRepository;
import org.dcsa.ebl.repository.DocumentPartyRepository;
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
    private final DisplayedAddressRepository displayedAddressRepository;

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
        return documentPartyRepository.findByPartyIDAndPartyFunctionAndShippingInstructionIDAndShipmentID(
                update.getPartyID(),
                update.getPartyFunction(),
                update.getShippingInstructionID(),
                update.getShipmentID()
        )
                .flatMap(current -> this.preUpdateHook(current, update))
                .flatMap(this::save);
    }

    public Mono<Void> deleteObsoleteDocumentPartyInstances(UUID shippingInstructionID) {
        return displayedAddressRepository.deleteByShippingInstructionIDAndShipmentIDIsNotNull(shippingInstructionID)
                .then(displayedAddressRepository.clearShippingInstructionIDWhereShipmentIDIsNotNull(shippingInstructionID))
                .thenMany(documentPartyRepository.findAllByShippingInstructionID(shippingInstructionID))
                .groupBy(documentParty -> documentParty.getShipmentID() != null)
                .concatMap(documentPartyGroupedFlux -> {
                    if (documentPartyGroupedFlux.key()) {
                        // Has a shipment ID, clear the shipping instruction ID but leave the entry
                        return documentPartyGroupedFlux.flatMap(original -> {
                            DocumentParty update = MappingUtil.instanceFrom(original, DocumentParty::new, DocumentParty.class);
                            update.setShippingInstructionID(null);
                            return this.preUpdateHook(original, update);
                        })
                                .concatMap(this::preSaveHook)
                                .buffer(Util.SQL_LIST_BUFFER_SIZE)
                                .concatMap(documentPartyRepository::saveAll);
                    }
                    // No shipment ID, delete them
                    return documentPartyGroupedFlux
                            .buffer(Util.SQL_LIST_BUFFER_SIZE)
                            .concatMap(documentParties ->
                                documentPartyRepository.deleteAll(documentParties)
                                        .thenMany(Flux.fromIterable(documentParties))
                                        .filter(documentParty -> documentParty.getPartyContactDetailsID() != null)
                                        .map(DocumentParty::getPartyContactDetailsID)
                                        .flatMap(partyContactDetailsService::deleteById)
                            );

                })
                .then();
    }

}
