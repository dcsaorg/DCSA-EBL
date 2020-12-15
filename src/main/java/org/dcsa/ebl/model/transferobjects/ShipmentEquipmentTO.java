package org.dcsa.ebl.model.transferobjects;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.dcsa.ebl.model.ActiveReeferSettings;
import org.dcsa.ebl.model.Seal;

import javax.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

/* Note: This is very distinct ShipmentEquipment */
@NoArgsConstructor
@Data
public class ShipmentEquipmentTO {

    private UUID shipmentEquipmentID;

    @Size(max = 15)
    private String equipmentReference;

    private String verifiedGrossMass;

    @Size(max = 20)
    private String weightUnit;

    private Float totalContainerWeight;

    private Integer partLoad;

    private ActiveReeferSettings activeReeferSettings;

    private List<Seal> seals;
}
