package org.dcsa.ebl.service;

import org.dcsa.core.service.ExtendedBaseService;
import org.dcsa.ebl.model.DisplayedAddress;
import org.dcsa.ebl.model.DocumentParty;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface DisplayedAddressService extends ExtendedBaseService<DisplayedAddress, UUID> {

    Mono<List<String>> loadDisplayedAddress(DocumentParty documentParty);
    Flux<DisplayedAddress> createDisplayedAddresses(DocumentParty documentParty, List<String> displayedAddressesAsStrings);
    Mono<Void> deleteAllDisplayedAddressesForShippingInstruction(String shippingInstructionID);
}
