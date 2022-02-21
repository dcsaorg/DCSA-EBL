package org.dcsa.ebl.service;

import org.dcsa.core.events.model.TransportDocument;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.core.service.ExtendedBaseService;
import org.dcsa.ebl.model.transferobjects.TransportDocumentTO;
import reactor.core.publisher.Mono;

public interface TransportDocumentService extends ExtendedBaseService<TransportDocument, String> {
    Mono<TransportDocumentTO> findByTransportDocumentReference(String transportDocumentReference);
    Mono<TransportDocumentTO> ApproveTransportDocument(String transportDocumentReference);

}
