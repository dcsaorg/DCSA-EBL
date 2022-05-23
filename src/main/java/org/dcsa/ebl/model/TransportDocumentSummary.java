package org.dcsa.ebl.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dcsa.skernel.model.enums.CarrierCodeListProvider;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.core.validator.EnumSubset;
import org.dcsa.skernel.model.transferobjects.PartyTO;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
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
  private String shippingInstructionReference;

  @EnumSubset(anyOf = EBL_DOCUMENT_STATUSES)
  private ShipmentEventTypeCode documentStatus;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  protected OffsetDateTime transportDocumentCreatedDateTime;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  protected OffsetDateTime transportDocumentUpdatedDateTime;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  private LocalDate issueDate;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  private LocalDate shippedOnboardDate;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  private LocalDate receivedForShipmentDate;

  @Size(max = 4)
  private String carrierCode;

  private CarrierCodeListProvider carrierCodeListProvider;

  @Size(max = 3)
  private String declaredValueCurrency;

  private Float declaredValue;

  private Integer numberOfRiderPages;

  private List<String> carrierBookingReferences;

  @NotNull private PartyTO issuingParty;
}
