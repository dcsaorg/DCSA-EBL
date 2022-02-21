package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.events.model.TransportDocument;
import org.dcsa.core.events.repository.TransportDocumentRepository;
import org.dcsa.core.service.impl.QueryServiceImpl;
import org.dcsa.ebl.model.transferobjects.TransportDocumentTO;
import org.dcsa.ebl.service.TransportDocumentService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class TransportDocumentServiceImpl extends QueryServiceImpl<TransportDocumentRepository, TransportDocument, String> implements TransportDocumentService {


    private final TransportDocumentRepository transportDocumentRepository;

  @Override
  public TransportDocumentRepository getRepository() {
    return transportDocumentRepository;
  }

    @Override
    public Mono<TransportDocumentTO> findById(String transportDocumentReference) {
        return null;
    }
}
