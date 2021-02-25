package org.dcsa.ebl.model.transferobjects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.dcsa.ebl.model.Clause;
import org.dcsa.ebl.model.Location;
import org.dcsa.ebl.model.TransportPlan;
import org.dcsa.ebl.model.base.AbstractTransportDocument;
import org.dcsa.ebl.model.enums.ServiceType;
import org.dcsa.ebl.model.enums.ShipmentTerm;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TransportDocumentTO extends AbstractTransportDocument {

    @Valid
    @JsonProperty("placeOfIssue")
    private Location placeOfIssueLocation;

    @Valid
    private ShipmentTerm shipmentTermAtOrigin;

    @Valid
    private ShipmentTerm shipmentTermAtDestination;

    @Valid
    private ServiceType serviceTypeAtOrigin;

    @Valid
    private ServiceType serviceTypeAtDestination;

    @Valid
    @Size(max = 30)
    private String serviceContract;

    @Valid
    private ShippingInstructionTO shippingInstruction;

    @Valid
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<ChargeTO> charges;

    @Valid
    private List<Clause> clauses;

    @Valid
    private TransportPlan transportPlan;
}
