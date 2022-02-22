package org.dcsa.ebl.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.dcsa.core.events.edocumentation.model.transferobject.CarrierClauseTO;
import org.dcsa.core.events.model.Location;
import org.dcsa.core.events.model.enums.CarrierCodeListProvider;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.core.events.model.transferobjects.ShippingInstructionTO;
import org.dcsa.core.validator.EnumSubset;
import org.dcsa.ebl.model.TransportPlan;
import org.dcsa.ebl.model.base.AbstractTransportDocument;
import org.dcsa.ebl.model.enums.ServiceType;
import org.dcsa.ebl.model.enums.ShipmentTerm;
import org.dcsa.ebl.model.transferobjects.ChargeTO;
import org.springframework.data.relational.core.mapping.Column;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.dcsa.core.events.model.enums.ShipmentEventTypeCode.EBL_DOCUMENT_STATUSES;

@Data
@NoArgsConstructor
public class TransportDocumentSummary {

  @NotNull
  @Size(max = 20)
  private String transportDocumentReference;

  @NotNull
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String shippingInstructionID;

  @EnumSubset(anyOf = EBL_DOCUMENT_STATUSES)
  private ShipmentEventTypeCode documentStatus;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  protected OffsetDateTime transportDocumentRequestCreatedDateTime;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  protected OffsetDateTime transportDocumentRequestUpdatedDateTime;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  private LocalDate issueDate;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  private LocalDate shippedOnboardDate;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  private LocalDate receivedForShipmentDate;

  @Size(max = 4)
  private String issuerCode;

  private CarrierCodeListProvider issuerCodeListProvider;

  @Size(max = 3)
  private String declaredValueCurrency;

  private Float declaredValue;

  private Integer numberOfRiderPages;

  private List<String> carrierBookingReferences;
}
