package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.dcsa.ebl.model.Location;
import org.dcsa.ebl.repository.LocationRepository;
import org.dcsa.ebl.service.LocationService;
import org.springframework.stereotype.Service;

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
}
