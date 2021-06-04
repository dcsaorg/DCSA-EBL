package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.dcsa.ebl.Util;
import org.dcsa.ebl.model.Address;
import org.dcsa.ebl.repository.AddressRepository;
import org.dcsa.ebl.service.AddressService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class AddressServiceImpl extends ExtendedBaseServiceImpl<AddressRepository, Address, UUID> implements AddressService {
    private final AddressRepository addressRepository;

    @Override
    public AddressRepository getRepository() {
        return addressRepository;
    }

    @Override
    public Mono<Address> ensureResolvable(Address address) {
        return Util.createOrFindByContent(address, addressRepository::findByContent, this::create);
    }
}
