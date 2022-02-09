package org.dcsa.ebl.model.transferobjects;

import lombok.Data;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.core.validator.EnumSubset;

import java.time.OffsetDateTime;

import static org.dcsa.core.events.model.enums.ShipmentEventTypeCode.EBL_DOCUMENT_STATUSES;

@Data
public class ShippingInstructionResponseTO {
  private String shippingInstructionID;

  @EnumSubset(anyOf = EBL_DOCUMENT_STATUSES)
  private ShipmentEventTypeCode documentStatus;

  private OffsetDateTime shippingInstructionCreatedDateTime;

  private OffsetDateTime shippingInstructionUpdatedDateTime;
}
