package org.dcsa.ebl.service;

import org.dcsa.ebl.model.transferobjects.ShippingInstructionResponseTO;
import org.dcsa.ebl.model.transferobjects.ShippingInstructionTO;
import reactor.core.publisher.Mono;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.ebl.model.transferobjects.ShippingInstructionSummaryTO;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;

import java.util.List;

public interface ShippingInstructionService {
  Mono<ShippingInstructionTO> findById(String shippingInstructionID);

  Mono<ShippingInstructionResponseTO> createShippingInstruction(ShippingInstructionTO shippingInstructionTO);
  Mono<ShippingInstructionTO> replaceOriginal(String shippingInstructionID, ShippingInstructionTO update);
  Flux<ShippingInstructionSummaryTO> findShippingInstructions(List<String> carrierBookingReferences, ShipmentEventTypeCode documentStatus, Pageable pageable);
}
