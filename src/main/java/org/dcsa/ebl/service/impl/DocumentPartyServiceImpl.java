package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.dcsa.ebl.model.DocumentParty;
import org.dcsa.ebl.repository.DocumentPartyRepository;
import org.dcsa.ebl.service.DocumentPartyService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

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
}
