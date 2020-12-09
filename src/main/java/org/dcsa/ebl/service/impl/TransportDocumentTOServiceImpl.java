package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.dcsa.ebl.model.transferobjects.ChargeTO;
import org.dcsa.ebl.model.EBLEndorsementChain;
import org.dcsa.ebl.model.transferobjects.TransportDocumentTO;
import org.dcsa.ebl.repository.TransportDocumentTORepository;
import org.dcsa.ebl.service.TransportDocumentTOService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class TransportDocumentTOServiceImpl extends ExtendedBaseServiceImpl<TransportDocumentTORepository, TransportDocumentTO, UUID> implements TransportDocumentTOService {
    private final TransportDocumentTORepository transportDocumentTORepository;


    @Override
    public TransportDocumentTORepository getRepository() {
        return transportDocumentTORepository;
    }

    @Override
    public Class<TransportDocumentTO> getModelClass() {
        return TransportDocumentTO.class;
    }

    @Override
    public Flux<ChargeTO> updateCharges(UUID ID, List<ChargeTO> chargeList) {
        return null;
    }

    @Override
    public Flux<EBLEndorsementChain> updateEBLEndorsementChain(UUID ID, List<EBLEndorsementChain> eblEndorsementChainList) {
        return null;
    }
}
