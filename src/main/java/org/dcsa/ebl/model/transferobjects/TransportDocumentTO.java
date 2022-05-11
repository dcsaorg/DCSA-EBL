package org.dcsa.ebl.model.transferobjects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dcsa.core.events.edocumentation.model.transferobject.CarrierClauseTO;
import org.dcsa.core.events.edocumentation.model.transferobject.ChargeTO;
import org.dcsa.core.events.edocumentation.model.transferobject.TransportTO;
import org.dcsa.skernel.model.enums.CarrierCodeListProvider;
import org.dcsa.core.events.model.transferobjects.ShippingInstructionTO;
import org.dcsa.skernel.model.transferobjects.LocationTO;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class TransportDocumentTO {

  @Size(max = 20)
  private String transportDocumentReference;

  private OffsetDateTime transportDocumentCreatedDateTime;

  private OffsetDateTime transportDocumentUpdatedDateTime;

  private LocalDate issueDate;

  private LocalDate shippedOnBoardDate;

  private LocalDate receivedForShipmentDate;

  private Integer numberOfOriginals;

  @Size(max = 4)
  private String issuerCode;

  private CarrierCodeListProvider issuerCodeListProvider;

  @Size(max = 3, message = "Declared Value Currency has a max size of 3.")
  private String declaredValueCurrency;

  private Double declaredValue;

  private Integer numberOfRiderPages;

  @Valid private LocationTO placeOfIssue;

  @Valid
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private ShippingInstructionTO shippingInstruction;

  @Valid
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private List<ChargeTO> charges;

//  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  @Valid private List<CarrierClauseTO> carrierClauses;

  @Valid
  private List<TransportTO> transports;
}
