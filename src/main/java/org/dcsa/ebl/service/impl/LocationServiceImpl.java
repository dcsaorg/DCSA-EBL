package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.dcsa.ebl.Util;
import org.dcsa.ebl.model.Address;
import org.dcsa.ebl.model.Location;
import org.dcsa.ebl.model.transferobjects.LocationTO;
import org.dcsa.ebl.repository.LocationRepository;
import org.dcsa.ebl.service.AddressService;
import org.dcsa.ebl.service.LocationService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class LocationServiceImpl extends ExtendedBaseServiceImpl<LocationRepository, Location, String> implements LocationService {
    private final LocationRepository locationRepository;
    private final AddressService addressService;

    @Override
    public LocationRepository getRepository() {
        return locationRepository;
    }

    public Mono<LocationTO> findPaymentLocationByShippingInstructionID(String shippingInstructionID) {
        return locationRepository.findPaymentLocationByShippingInstructionID(shippingInstructionID)
                .flatMap(this::getLocationTO);
    }

    @Override
    public Mono<LocationTO> ensureResolvable(LocationTO locationTO) {
        Address address = locationTO.getAddress();
        Mono<LocationTO> locationTOMono;
        if (address != null) {
            locationTOMono = addressService.ensureResolvable(address)
                    .doOnNext(locationTO::setAddress)
                    .thenReturn(locationTO);
        } else {
            locationTOMono = Mono.just(locationTO);
        }

        return locationTOMono
            .flatMap(locTo -> Util.createOrFindByContent(
                locTo,
                locationRepository::findByContent,
                locTO -> this.create(locTO.toLocation())
        )).map(location -> location.toLocationTO(locationTO.getAddress()));
    }

    @Override
    public Mono<LocationTO> findTOById(String locationID) {
        return findById(locationID)
                .flatMap(this::getLocationTO);
    }

    private Mono<LocationTO> getLocationTO(Location location) {
        if (location.getAddressID() != null) {
            return addressService.findById(location.getAddressID())
                    .map(location::toLocationTO);
        }
        return Mono.just(location.toLocationTO(null));
    }
}
