package org.dcsa.ebl.model.transferobjects;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dcsa.ebl.model.CargoLineItem;
import org.dcsa.ebl.model.base.AbstractCargoItem;

import javax.validation.constraints.Size;
import java.util.List;

@NoArgsConstructor
@Data
public class CargoItemTO extends AbstractCargoItem {
    private List<CargoLineItem> cargoLineItems;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Size(max = 15)
    private String equipmentReference;

}
