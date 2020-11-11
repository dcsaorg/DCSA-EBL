package org.dcsa.ebl.model.transferobjects;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dcsa.ebl.model.CargoItem;
import reactor.util.function.Tuple2;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
public class ShippingInstructionTO {

    @JsonProperty("shippingInstructionID")
    private UUID id;

    @NotNull
    private UUID carrierBookingReference;

    @NotNull
    private List<Tuple2<CargoItem, String>> cargoItemEquipmentReferenceTuple;

    private Boolean isElectronic;

    private Integer numberOfRiderPages ;

    @NotNull
    private String callBackUrl;
}


