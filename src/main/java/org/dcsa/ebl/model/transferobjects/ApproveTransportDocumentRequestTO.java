package org.dcsa.ebl.model.transferobjects;
import lombok.Data;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.core.validator.EnumSubset;
import javax.validation.constraints.NotNull;

@Data
public class ApproveTransportDocumentRequestTO {

    @NotNull(message = "DocumentStatus is required")
    @EnumSubset(anyOf = "APPR")
    private ShipmentEventTypeCode documentStatus;
}
