package org.dcsa.ebl.model.transferobjects;

import lombok.Builder;
import lombok.Data;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
public class ShippingInstructionSummaryTO {
    @NotNull
    @Size(max = 100)
    private String shippingInstructionReference;

    private ShipmentEventTypeCode documentStatus;

    private OffsetDateTime createdDateTime;

    private OffsetDateTime updatedDateTime;

    private String transportDocumentTypeCode;

    @NotNull
    private Boolean isShippedOnboardType;

    private Integer numberOfCopies;

    private Integer numberOfOriginals;

    @NotNull
    private Boolean isElectronic;

    @NotNull
    private Boolean isToOrder;

    private Boolean areChargesDisplayedOnOriginals;

    private Boolean areChargesDisplayedOnCopies;

    @Size(max = 250)
    private String displayedNameForPlaceOfReceipt;

    @Size(max = 250)
    private String displayedNameForPortOfLoad;

    @Size(max = 250)
    private String displayedNameForPortOfDischarge;

    @Size(max = 250)
    private String displayedNameForPlaceOfDelivery;

    private List<String> carrierBookingReferences;
}
