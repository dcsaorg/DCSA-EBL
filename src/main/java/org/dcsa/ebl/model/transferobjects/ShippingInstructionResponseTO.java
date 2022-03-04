package org.dcsa.ebl.model.transferobjects;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.core.validator.EnumSubset;

import java.time.OffsetDateTime;

import static org.dcsa.core.events.model.enums.ShipmentEventTypeCode.EBL_DOCUMENT_STATUSES;

@Data
public class ShippingInstructionResponseTO {
  private String shippingInstructionReference;

  @EnumSubset(anyOf = EBL_DOCUMENT_STATUSES)
  private ShipmentEventTypeCode documentStatus;

  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private OffsetDateTime shippingInstructionCreatedDateTime;

  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private OffsetDateTime shippingInstructionUpdatedDateTime;
}
