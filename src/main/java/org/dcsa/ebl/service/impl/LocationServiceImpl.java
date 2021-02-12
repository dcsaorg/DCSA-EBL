package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.exception.UpdateException;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.dcsa.ebl.model.Location;
import org.dcsa.ebl.repository.LocationRepository;
import org.dcsa.ebl.service.LocationService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class LocationServiceImpl extends ExtendedBaseServiceImpl<LocationRepository, Location, UUID> implements LocationService {
    private final LocationRepository locationRepository;

    @Override
    public LocationRepository getRepository() {
        return locationRepository;
    }

    @Override
    public Class<Location> getModelClass() {
        return Location.class;
    }

    public Mono<Location> findPaymentLocationByShippingInstructionID(UUID shippingInstructionID) {
        return locationRepository.findPaymentLocationByShippingInstructionID(shippingInstructionID);
    }

    public Mono<Location> resolveLocation(Location location) {
        Mono<Location> locationMono;
        if (location.getId() != null) {
            locationMono = findById(location.getId())
                    .flatMap(resolvedLocation -> {
                        if (!location.containsOnlyID() && !resolvedLocation.equals(location)) {
                            return Mono.error(new UpdateException("Location with id " + location.getId()
                                    + " exists but has different content. Remove the locationID field to"
                                    + " create a new instance or provide an update"));
                        }
                        return Mono.just(resolvedLocation);
                    });
        } else {
            locationMono = create(location);
        }
        return locationMono.doOnNext(resolvedLocation -> location.setId(resolvedLocation.getId()));
    }
}
