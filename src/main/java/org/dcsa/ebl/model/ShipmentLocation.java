package org.dcsa.ebl.model;

import org.dcsa.core.model.AuditBase;
import org.dcsa.core.model.GetId;

import java.util.UUID;

public class ShipmentLocation extends AuditBase implements GetId<UUID> {
    shipment_id uuid NOT NULL,
    location_id uuid NOT NULL,
    location_type varchar(3) NOT NULL,
    displayed_name varchar(250)
}
