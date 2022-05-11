package org.dcsa.ebl.model.transferobjects;

import lombok.Data;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.core.validator.EnumSubset;

import javax.validation.constraints.Size;
import java.time.OffsetDateTime;

import static org.dcsa.core.events.model.enums.ShipmentEventTypeCode.EBL_DOCUMENT_STATUSES;

@Data
public class TransportDocumentRefStatusTO {
  @Size(max = 20)
  private String transportDocumentReference;

  @EnumSubset(anyOf = EBL_DOCUMENT_STATUSES)
  private ShipmentEventTypeCode documentStatus;

  private OffsetDateTime transportDocumentCreatedDateTime;

  private OffsetDateTime transportDocumentUpdatedDateTime;

}
