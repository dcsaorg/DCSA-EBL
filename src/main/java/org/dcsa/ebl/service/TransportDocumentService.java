package org.dcsa.ebl.service;

import org.dcsa.core.events.model.TransportDocument;
import org.dcsa.core.service.QueryService;
import org.dcsa.ebl.model.transferobjects.TransportDocumentTO;
import reactor.core.publisher.Mono;

public interface TransportDocumentService extends QueryService<TransportDocument, String> {

  Mono<TransportDocumentTO> findByTransportDocumentReference(String transportDocumentReference);
}
