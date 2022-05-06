package org.dcsa.ebl.model.transferobjects;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.core.validator.EnumSubset;

import java.time.OffsetDateTime;

import static org.dcsa.core.events.model.enums.ShipmentEventTypeCode.EBL_DOCUMENT_STATUSES;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor
public class ShippingInstructionResponseTO {
  String shippingInstructionReference;
  @EnumSubset(anyOf = EBL_DOCUMENT_STATUSES)
  ShipmentEventTypeCode documentStatus;

  @JsonFormat(shape = JsonFormat.Shape.STRING)
  OffsetDateTime shippingInstructionCreatedDateTime;

  @JsonFormat(shape = JsonFormat.Shape.STRING)
  OffsetDateTime shippingInstructionUpdatedDateTime;
}
