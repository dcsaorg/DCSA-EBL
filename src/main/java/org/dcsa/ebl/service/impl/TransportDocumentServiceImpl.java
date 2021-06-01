package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.dcsa.ebl.model.TransportDocument;
import org.dcsa.ebl.repository.TransportDocumentRepository;
import org.dcsa.ebl.service.TransportDocumentService;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TransportDocumentServiceImpl extends ExtendedBaseServiceImpl<TransportDocumentRepository, TransportDocument, String> implements TransportDocumentService {

    private final TransportDocumentRepository transportDocumentRepository;

    @Override
    public TransportDocumentRepository getRepository() {
        return transportDocumentRepository;
    }
}
