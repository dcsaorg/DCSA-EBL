package org.dcsa.ebl.model.transferobjects;

import lombok.Data;
import org.dcsa.core.model.AuditBase;
import org.dcsa.core.model.GetId;

import java.util.UUID;

@Data
public class ShipmentTO extends AuditBase implements GetId<UUID> {
    private UUID id;
}
