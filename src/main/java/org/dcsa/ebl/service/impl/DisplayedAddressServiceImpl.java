package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.events.model.DisplayedAddress;
import org.dcsa.core.events.model.DocumentParty;
import org.dcsa.core.events.repository.DisplayedAddressRepository;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.dcsa.ebl.service.DisplayedAddressService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class DisplayedAddressServiceImpl extends ExtendedBaseServiceImpl<DisplayedAddressRepository, DisplayedAddress, UUID> implements DisplayedAddressService {
    private final DisplayedAddressRepository displayedAddressRepository;

    @Override
    public DisplayedAddressRepository getRepository() {
        return displayedAddressRepository;
    }

    public Mono<List<String>> loadDisplayedAddress(DocumentParty documentParty) {
        return Mono.empty();
    }

    @Override
    public Flux<DisplayedAddress> createDisplayedAddresses(DocumentParty documentParty, List<String> displayedAddressesAsStrings) {
        return Flux.empty();
    }

    @Override
    public Mono<Void> deleteAllDisplayedAddressesForShippingInstruction(String shippingInstructionID) {
        return Mono.empty();
    }
}
