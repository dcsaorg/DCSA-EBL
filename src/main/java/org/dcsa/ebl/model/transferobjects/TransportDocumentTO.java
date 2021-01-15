package org.dcsa.ebl.model.transferobjects;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.dcsa.ebl.model.Charge;
import org.dcsa.ebl.model.TransportDocument;
import org.springframework.data.annotation.Transient;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TransportDocumentTO extends TransportDocument {

    @NotNull
    @Transient
    @Valid
    private ShippingInstructionTO shippingInstruction;

    @NotNull
    @Transient
    @Valid
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Charge> charges;
}
