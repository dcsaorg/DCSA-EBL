package org.dcsa.ebl.repository;

import org.dcsa.core.repository.ExtendedRepository;
import org.dcsa.ebl.model.DocumentParty;
import org.dcsa.ebl.model.enums.PartyFunction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface DocumentPartyRepository extends ExtendedRepository<DocumentParty, UUID> {
    Flux<DocumentParty> findAllByShippingInstructionID(String shippingInstructionID);
    Mono<DocumentParty> findByPartyIDAndPartyFunctionAndShippingInstructionIDAndShipmentID(
            String partyID,
            PartyFunction partyFunction,
            String shippingInstructionID,
            UUID shipmentID
    );
}
