package org.dcsa.ebl.repository;

import org.dcsa.core.repository.ExtendedRepository;
import org.dcsa.ebl.model.Address;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface AddressRepository extends ExtendedRepository<Address, UUID> {

    Mono<Address> findByNameAndStreetAndStreetNumberAndFloorAndPostalCodeAndCityNameAndStateRegionAndCountry(
            String name,
            String street,
            String streetNumber,
            String floor,
            String postalCode,
            String cityName,
            String stateRegion,
            String country
    );

    default Mono<Address> findByContent(Address address) {
        if (address.getId() != null) {
            return findById(address.getId());
        }
        return findByNameAndStreetAndStreetNumberAndFloorAndPostalCodeAndCityNameAndStateRegionAndCountry(
                address.getName(),
                address.getStreet(),
                address.getStreetNumber(),
                address.getFloor(),
                address.getPostalCode(),
                address.getCityName(),
                address.getStateRegion(),
                address.getCountry()
        );
    }
}
