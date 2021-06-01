package org.dcsa.ebl.repository;

import org.dcsa.core.repository.ExtendedRepository;
import org.dcsa.ebl.model.Location;
import org.springframework.data.r2dbc.repository.Query;
import reactor.core.publisher.Mono;

public interface LocationRepository extends ExtendedRepository<Location, String> {
    @Query("SELECT location.*"
            + "  FROM location"
            + "  JOIN shipping_instruction ON (location.id=shipping_instruction.invoice_payable_at)"
            + " WHERE shipping_instruction.id = :shippingInstructionID"
    )
    Mono<Location> findPaymentLocationByShippingInstructionID(String shippingInstructionID);
}
