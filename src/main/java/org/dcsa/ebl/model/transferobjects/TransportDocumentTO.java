package org.dcsa.ebl.model.transferobjects;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dcsa.core.model.AuditBase;
import org.dcsa.core.model.GetId;
import org.dcsa.core.util.ValidationUtils;
import org.dcsa.ebl.model.enums.DocumentStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

@Table("transport_document")
@Data
@NoArgsConstructor
public class TransportDocumentTO extends AuditBase implements GetId<UUID> {

    @Id
    @JsonProperty("transportDocumentID")
    private UUID id;

    @NotNull
    private UUID shippingInstructionID;

    @NotNull
    private DocumentStatus documentStatus;

    @NotNull
    @Size(max = 7)
    private String vesselIMONumber;

    public void setVesselIMONumber(String vesselIMONumber) {
        ValidationUtils.validateVesselIMONumber(vesselIMONumber);
        this.vesselIMONumber = vesselIMONumber;
    }

    @NotNull
    private Integer totalContainerWeight;

    @NotNull
    private LocalDate onboardDate;

    @NotNull
    private LocalDate shippedOnboardDate;

    @NotNull
    private String termsAndConditions;

    private UUID placeOfIssue;

    @Size(max = 20)
    private String chargeType;

    private LocalDate receivedForShipmentDate;

    @Size(max = 500)
    private String signature;
}
