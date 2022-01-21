package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.events.model.DisplayedAddress;
import org.dcsa.core.events.model.DocumentParty;
import org.dcsa.core.events.repository.DisplayedAddressRepository;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.dcsa.ebl.Util;
import org.dcsa.ebl.model.utils.MappingUtil;
import org.dcsa.ebl.service.DisplayedAddressService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
        // TODO: fix me
//        return displayedAddressRepository.findByPartyIDAndPartyFunctionAndShippingInstructionIDAndShipmentIDOrderByAddressLineNumberAsc(
//                documentParty.getPartyID(),
//                documentParty.getPartyFunction(),
//                documentParty.getShippingInstructionID(),
//                documentParty.getShipmentID()
//        )
//        .map(DisplayedAddress::getAddressLine)
//        .collectList();
    }

    @Override
    public Flux<DisplayedAddress> createDisplayedAddresses(DocumentParty documentParty, List<String> displayedAddressesAsStrings) {
        return Flux.empty();
        // TODO: fix me
//        DisplayedAddress base;
//        List<DisplayedAddress> displayedAddressList;
//        if (displayedAddressesAsStrings == null || displayedAddressesAsStrings.isEmpty()) {
//            return Flux.empty();
//        }
//        base = new DisplayedAddress();
//        base.setPartyID(Objects.requireNonNull(documentParty.getPartyID(), "partyID"));
//        base.setPartyFunction(documentParty.getPartyFunction());
//        base.setShipmentID(documentParty.getShipmentID());
//        base.setShippingInstructionID(documentParty.getShippingInstructionID());
//        displayedAddressList = new ArrayList<>(displayedAddressesAsStrings.size());
//        for (int i = 0 ; i < displayedAddressesAsStrings.size() ; i++) {
//            DisplayedAddress displayedAddress = MappingUtil.instanceFrom(base, DisplayedAddress::new, DisplayedAddress.class);
//            displayedAddress.setAddressLine(displayedAddressesAsStrings.get(i));
//            displayedAddress.setAddressLineNumber(i);
//            displayedAddressList.add(displayedAddress);
//        }
//        return Flux.fromIterable(displayedAddressList)
//                .concatMap(this::preCreateHook)
//                .concatMap(this::preSaveHook)
//                .buffer(Util.SQL_LIST_BUFFER_SIZE)
//                .concatMap(displayedAddressRepository::saveAll);
//
    }

    @Override
    public Mono<Void> deleteAllDisplayedAddressesForShippingInstruction(String shippingInstructionID) {
        return Mono.empty();
        // TODO: fix me
//
//        return displayedAddressRepository.deleteByShippingInstructionIDAndShipmentIDIsNull(shippingInstructionID)
//                .then(displayedAddressRepository.clearShippingInstructionIDWhereShipmentIDIsNotNull(shippingInstructionID))
//                .then();
    }
}
