package org.dcsa.ebl.model.transferobjects;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.core.validator.EnumSubset;

import javax.validation.constraints.NotNull;

@Builder
@Value
@Jacksonized
public class ApproveTransportDocumentRequestTO {

  @NotNull(message = "DocumentStatus is required")
  @EnumSubset(anyOf = "APPR")
  ShipmentEventTypeCode documentStatus;
}
