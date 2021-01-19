package org.dcsa.ebl.model.transferobjects;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.dcsa.ebl.model.CargoLineItem;
import org.dcsa.ebl.model.base.AbstractCargoItem;
import org.springframework.data.annotation.Transient;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class CargoItemTO extends AbstractCargoItem {
    private List<CargoLineItem> cargoLineItems;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Size(max = 15)
    private String equipmentReference;

    @NotNull
    @Transient
    private String carrierBookingReference;

}
