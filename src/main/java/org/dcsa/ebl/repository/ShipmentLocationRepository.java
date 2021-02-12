package org.dcsa.ebl.repository;

import org.dcsa.core.repository.ExtendedRepository;
import org.dcsa.ebl.model.ShipmentLocation;

import java.util.UUID;

public interface ShipmentLocationRepository extends ExtendedRepository<ShipmentLocation, UUID>, InsertAddonRepository<ShipmentLocation> {

}
