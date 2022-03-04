package org.dcsa.ebl.service;

import org.dcsa.core.events.model.TransportDocument;
import org.dcsa.core.service.AsymmetricQueryService;
import org.dcsa.ebl.model.TransportDocumentSummary;
import org.dcsa.ebl.model.transferobjects.TransportDocumentTO;
import reactor.core.publisher.Mono;

public interface TransportDocumentService
    extends AsymmetricQueryService<TransportDocument, TransportDocumentSummary, String> {
  Mono<TransportDocumentTO> findById(String transportDocumentReference);

  Mono<TransportDocumentTO> findByTransportDocumentReference(String transportDocumentReference);
}
