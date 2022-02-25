package org.dcsa.ebl.service;

import org.dcsa.core.events.model.TransportDocument;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.core.service.AsymmetricQueryService;
import org.dcsa.core.service.QueryService;
import org.dcsa.ebl.model.TransportDocumentSummary;
import org.dcsa.ebl.model.transferobjects.TransportDocumentTO;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface TransportDocumentService extends AsymmetricQueryService<TransportDocument, TransportDocumentSummary, String> {
  Mono<TransportDocumentTO> findById(String transportDocumentReference);
}
