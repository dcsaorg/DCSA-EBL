package org.dcsa.ebl.model.transferobjects;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.dcsa.ebl.model.ActiveReeferSettings;
import org.dcsa.ebl.model.Seal;
import org.dcsa.ebl.model.base.AbstractShipmentEquipment;

import javax.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class ShipmentEquipmentTO extends AbstractShipmentEquipment {

    private UUID shipmentEquipmentID;

    private ActiveReeferSettings activeReeferSettings;

    private List<Seal> seals;

}
