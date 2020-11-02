package org.dcsa.ebl.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dcsa.core.model.AuditBase;
import org.dcsa.core.model.GetId;
import org.dcsa.ebl.model.enums.TransportDocumentType;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("shipping_instruction")
@Data
@NoArgsConstructor
public class ShippingInstruction extends AuditBase implements GetId<UUID> {

    @Id
    @JsonProperty("shippingInstructionID")
    @Column("shipping_instruction_number")
    private UUID id;


    @Column("carrier_booking_number")
    private String carrierBookingNumber;

    @Column("transport_document_type")
    private TransportDocumentType transportDocumentType;

    @Column("transport_reference")
    private UUID transportReference;

    @Column("carrierVoyageNumber")
    private UUID carrierVoyageNumber;


    @JsonProperty("shipmentID")
    @Column("shipment_id")
    private UUID shipmentId;
}
