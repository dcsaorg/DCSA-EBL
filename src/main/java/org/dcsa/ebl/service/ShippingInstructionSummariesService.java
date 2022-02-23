package org.dcsa.ebl.service;

import org.dcsa.core.events.model.ShippingInstruction;
import org.dcsa.core.extendedrequest.ExtendedRequest;
import org.dcsa.ebl.model.transferobjects.ShippingInstructionSummaryTO;
import reactor.core.publisher.Flux;

public interface ShippingInstructionSummariesService {
  Flux<ShippingInstructionSummaryTO> findShippingInstructionSummaries(ExtendedRequest<ShippingInstruction> request);
}
