package org.dcsa.ebl.model.transferobjects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.dcsa.ebl.model.*;
import org.dcsa.ebl.model.base.AbstractTransportDocument;
import org.dcsa.ebl.model.enums.ServiceType;
import org.dcsa.ebl.model.enums.ShipmentTerm;
import org.springframework.data.annotation.Transient;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TransportDocumentTO extends AbstractTransportDocument {

    @Transient
    @Valid
    @JsonProperty("placeOfIssue")
    private Location placeOfIssueLocation;

    @Transient
    @Valid
    private ShipmentTerm shipmentTermAtOrigin;

    @Transient
    @Valid
    private ShipmentTerm shipmentTermAtDestination;

    @Transient
    @Valid
    private ServiceType serviceTypeAtOrigin;

    @Transient
    @Valid
    private ServiceType serviceTypeAtDestination;

    @Transient
    @Valid
    @Size(max = 30)
    private String serviceContract;

    @Transient
    @Valid
    private ShippingInstructionTO shippingInstruction;

    @Transient
    @Valid
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<ChargeTO> charges;

    @Transient
    @Valid
    private List<Clause> clauses;

    @Transient
    @Valid
    private TransportPlan transportPlan;
}
