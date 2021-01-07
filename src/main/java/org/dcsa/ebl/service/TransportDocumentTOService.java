package org.dcsa.ebl.service;

import org.dcsa.core.extendedrequest.ExtendedRequest;
import org.dcsa.core.service.ExtendedBaseService;
import org.dcsa.ebl.model.ShippingInstruction;
import org.dcsa.ebl.model.TransportDocument;
import org.dcsa.ebl.model.transferobjects.ShippingInstructionTO;
import org.dcsa.ebl.model.transferobjects.TransportDocumentTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface TransportDocumentTOService {

    Mono<TransportDocumentTO> findById(UUID uuid);
    // Use of ShippingInstruction is deliberate because we do not support filtering on any other fields then
    // those provided in ShippingInstruction.class
    Flux<TransportDocumentTO> findAllExtended(final ExtendedRequest<TransportDocument> extendedRequest);
}
