package org.dcsa.ebl.repository;

import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.ebl.model.transferobjects.ShippingInstructionSummaryTO;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;

import java.util.List;

public interface ShippingInstructionCustomRepository {
    Flux<ShippingInstructionSummaryTO> findShippingInstructions(List<String> carrierBookingReferences, ShipmentEventTypeCode documentStatus, Pageable pageable);
}
