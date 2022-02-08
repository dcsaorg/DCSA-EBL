package org.dcsa.ebl.model.base;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.dcsa.core.events.model.enums.DocumentStatus;
import org.dcsa.core.model.AuditBase;
import org.dcsa.ebl.model.enums.TransportDocumentTypeCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.OffsetDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public abstract class AbstractShippingInstruction extends AuditBase {

  @Id
  @Column("id")
  private String shippingInstructionID;

  @Column("document_status")
  private DocumentStatus documentStatus;

  @Column("shipping_instruction_created_date_time")
  private OffsetDateTime shippingInstructionCreatedDateTime;

  @Column("shipping_instruction_updated_date_time")
  private OffsetDateTime shippingInstructionUpdatedDateTime;

  @NotNull
  @Column("is_shipped_onboard_type")
  private Boolean isShippedOnboardType;

  @Column("number_of_copies")
  private Integer numberOfCopies;

  @Column("number_of_originals")
  private Integer numberOfOriginals;

  @Column("is_electronic")
  private Boolean isElectronic;

  @NotNull
  @Column("is_to_order")
  private Boolean isToOrder;

  @NotNull
  @Column("are_charges_displayed_on_originals")
  private Boolean areChargesDisplayedOnOriginals;

  @NotNull
  @Column("are_charges_displayed_on_copies")
  private Boolean areChargesDisplayedOnCopies;

  @Column("place_of_issue")
  private String placeOfIssueID;

  @Column("transport_document_type_code")
  private TransportDocumentTypeCode transportDocumentTypeCode;

  @Size(max = 250)
  @Column("displayed_name_for_place_of_receipt")
  private String displayedNameForPlaceOfReceipt;

  @Size(max = 250)
  @Column("displayed_name_for_port_of_load")
  private String displayedNameForPortOfLoad;

  @Size(max = 250)
  @Column("displayed_name_for_port_of_discharge")
  private String displayedNameForPortOfDischarge;

  @Size(max = 250)
  @Column("displayed_name_for_place_of_delivery")
  private String displayedNameForPlaceOfDelivery;
}
