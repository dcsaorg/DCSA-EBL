package org.dcsa.ebl.repository;

import org.dcsa.core.repository.ExtendedRepository;
import org.dcsa.ebl.model.Address;
import org.springframework.data.relational.core.mapping.Column;
import reactor.core.publisher.Mono;

import javax.validation.constraints.Size;
import java.util.UUID;

public interface AddressRepository extends ExtendedRepository<Address, UUID> {

    Mono<Address> findByNameAndStreetAndStreetNumberAndFloorAndPostalCodeAndCityAndStateRegionAndCountry(
            String name,
            String street,
            String streetNumber,
            String floor,
            String postalCode,
            String city,
            String stateRegion,
            String country
    );

    default Mono<Address> findByContent(Address address) {
        if (address.getId() != null) {
            return findById(address.getId());
        }
        return findByNameAndStreetAndStreetNumberAndFloorAndPostalCodeAndCityAndStateRegionAndCountry(
                address.getName(),
                address.getStreet(),
                address.getStreetNumber(),
                address.getFloor(),
                address.getPostalCode(),
                address.getCity(),
                address.getStateRegion(),
                address.getCountry()
        );
    }
}
