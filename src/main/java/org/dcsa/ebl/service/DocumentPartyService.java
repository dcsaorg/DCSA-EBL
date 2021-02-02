package org.dcsa.ebl.service;

import org.dcsa.core.service.ExtendedBaseService;
import org.dcsa.ebl.model.DocumentParty;
import org.dcsa.ebl.model.enums.PartyFunction;
import org.dcsa.ebl.model.transferobjects.DocumentPartyTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface DocumentPartyService extends ExtendedBaseService<DocumentParty, UUID> {
    Flux<DocumentParty> findAllByShippingInstructionID(UUID shippingInstructionID);
    Mono<DocumentParty> findByPartyIDAndPartyFunction(UUID partyID, PartyFunction partyFunction);
    Mono<Integer> deleteByPartyIDAndPartyFunctionAndShipmentID(UUID partyID, PartyFunction partyFunction, UUID shipmentID);
    Mono<Void> deleteObsoleteDocumentPartyInstances(Iterable<DocumentPartyTO> documentPartyTOs);
}
