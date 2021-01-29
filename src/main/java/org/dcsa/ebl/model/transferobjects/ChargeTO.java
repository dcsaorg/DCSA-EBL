package org.dcsa.ebl.model.transferobjects;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.dcsa.ebl.model.Location;
import org.dcsa.ebl.model.base.AbstractCharge;
import org.springframework.data.annotation.Transient;

import javax.validation.Valid;

@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class ChargeTO extends AbstractCharge {
    @Transient
    @Valid
    @JsonProperty("freightPayableAt")
    private Location freightPayableAtLocation;
}
