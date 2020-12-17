package org.dcsa.ebl.model.transferobjects;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.dcsa.ebl.model.ActiveReeferSettings;
import org.dcsa.ebl.model.Seal;
import org.dcsa.ebl.model.base.AbstractShipmentEquipment;

import javax.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

/* Note: This is very distinct ShipmentEquipment */
@NoArgsConstructor
@Data
public class ShipmentEquipmentTO extends AbstractShipmentEquipment {

    private UUID shipmentEquipmentID;

    private ActiveReeferSettings activeReeferSettings;

    private List<Seal> seals;

}
