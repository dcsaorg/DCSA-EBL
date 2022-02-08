package org.dcsa.ebl.model.transferobjects;

import lombok.Data;
import org.dcsa.core.events.model.enums.DocumentStatus;

import java.time.OffsetDateTime;

@Data
public class ShippingInstructionResponseTO {
  private String shippingInstructionID;
  private DocumentStatus documentStatus;
  private OffsetDateTime shippingInstructionCreatedDateTime;
  private OffsetDateTime shippingInstructionUpdatedDateTime;
}
