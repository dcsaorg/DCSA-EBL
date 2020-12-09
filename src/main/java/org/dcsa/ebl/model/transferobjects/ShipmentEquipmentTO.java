package org.dcsa.ebl.model.transferobjects;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.dcsa.ebl.model.ActiveReeferSettings;
import org.dcsa.ebl.model.Seal;

import javax.validation.constraints.Size;
import java.util.List;

@NoArgsConstructor
@Data
public class ShipmentEquipmentTO {
    @Size(max = 15)
    private String equipmentReference;

    private Float verifiedGrossMass;

    @Size(max = 20)
    private String weightUnit;

    private Float totalContainerWeight;

    private Integer partLoad;

    private ActiveReeferSettings activeReeferSettings;

    private List<Seal> seals;
}
