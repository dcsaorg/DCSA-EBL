package org.dcsa.ebl.service;

import org.dcsa.core.events.model.TransportDocument;
import org.dcsa.core.service.AsymmetricQueryService;
import org.dcsa.ebl.model.TransportDocumentSummary;
import org.dcsa.ebl.model.transferobjects.TransportDocumentTO;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface TransportDocumentService
    extends AsymmetricQueryService<TransportDocument, TransportDocumentSummary, UUID> {
  Mono<TransportDocumentTO> findById(String transportDocumentReference);

  Mono<TransportDocumentTO> findByTransportDocumentReference(String transportDocumentReference);

  Mono<TransportDocumentTO> ApproveTransportDocument(String transportDocumentReference);
}
