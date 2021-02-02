package org.dcsa.ebl.model.transferobjects;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.dcsa.ebl.model.CargoLineItem;
import org.dcsa.ebl.model.base.AbstractCargoItem;

import javax.validation.constraints.Size;
import java.util.List;

@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class CargoItemTO extends AbstractCargoItem {
    private List<CargoLineItem> cargoLineItems;

    @Size(max = 15)
    private String equipmentReference;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String carrierBookingReference;

}
