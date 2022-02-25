package org.dcsa.ebl.model.transferobjects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.dcsa.core.events.edocumentation.model.transferobject.CarrierClauseTO;
import org.dcsa.core.events.model.AbstractTransportDocument;
import org.dcsa.core.events.model.Location;
import org.dcsa.core.events.model.transferobjects.ShippingInstructionTO;
import org.dcsa.ebl.model.TransportPlan;
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
    private List<CarrierClauseTO> clauses;

    @Valid
    private TransportPlan transportPlan;
}
