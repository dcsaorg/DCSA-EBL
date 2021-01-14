package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.extendedrequest.ExtendedRequest;
import org.dcsa.ebl.model.TransportDocument;
import org.dcsa.ebl.model.transferobjects.TransportDocumentTO;
import org.dcsa.ebl.service.TransportDocumentService;
import org.dcsa.ebl.service.TransportDocumentTOService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class TransportDocumentTOServiceImpl implements TransportDocumentTOService {
    private final TransportDocumentService transportDocumentService;

    @Transactional
    @Override
    public Mono<TransportDocumentTO> findById(UUID transportDocumentID) {
        return null;
    }

    @Override
    public Flux<TransportDocument> findAllExtended(final ExtendedRequest<TransportDocument> extendedRequest) {
        return transportDocumentService.findAllExtended(extendedRequest);
    }
}
