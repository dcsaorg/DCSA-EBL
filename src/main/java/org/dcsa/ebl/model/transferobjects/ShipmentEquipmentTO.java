package org.dcsa.ebl.model.transferobjects;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.dcsa.ebl.model.ActiveReeferSettings;
import org.dcsa.ebl.model.Seal;
import org.dcsa.ebl.model.base.AbstractShipmentEquipment;
import org.springframework.data.relational.core.mapping.Column;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class ShipmentEquipmentTO extends AbstractShipmentEquipment {

    @NotNull
    private EquipmentTO equipment;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ActiveReeferSettings activeReeferSettings;

    @Valid
    private List<Seal> seals;
}
