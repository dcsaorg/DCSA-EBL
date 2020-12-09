package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.dcsa.ebl.model.transferobjects.TransportDocumentTO;
import org.dcsa.ebl.repository.TransportDocumentTORepository;
import org.dcsa.ebl.service.TransportDocumentTOService;
import org.springframework.stereotype.Service;

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

}
