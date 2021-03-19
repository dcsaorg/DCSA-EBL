package org.dcsa.ebl.repository;

import org.dcsa.core.repository.ExtendedRepository;
import org.dcsa.ebl.model.ShipmentLocation;
import org.springframework.data.r2dbc.repository.Query;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface ShipmentLocationRepository extends ExtendedRepository<ShipmentLocation, UUID>, InsertAddonRepository<ShipmentLocation> {
    @Query("SELECT shipment_location.* FROM shipment_location"
            + " JOIN shipment ON (shipment_location.shipment_id=shipment.id)"
            + " WHERE shipment.carrier_booking_reference = :carrierBookingReference")
    Flux<ShipmentLocation> findAllByCarrierBookingReference(String carrierBookingReference);
}
