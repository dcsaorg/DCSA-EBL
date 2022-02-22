package org.dcsa.ebl.service;

import org.dcsa.core.events.model.TransportDocument;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.core.extendedrequest.ExtendedRequest;
import org.dcsa.ebl.model.TransportDocumentSummary;
import org.dcsa.ebl.model.transferobjects.TransportDocumentTO;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface TransportDocumentService {
  Mono<List<TransportDocumentSummary>> findByCarrierBookingReference(List<String> carrierBookingReferences, ShipmentEventTypeCode documentStatus, Pageable pageable);
  Mono<TransportDocumentTO> findById(String transportDocumentReference);
  Flux<TransportDocument> findAllExtended(final ExtendedRequest<TransportDocument> extendedRequest);

}
