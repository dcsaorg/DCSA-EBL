package org.dcsa.ebl.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.dcsa.core.model.AuditBase;
import org.dcsa.core.model.GetId;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

@Table("shipment")
@Data
@NoArgsConstructor
public class Shipment extends AuditBase implements GetId<UUID> {
    @Id
    private UUID id;

    @Column("transport_document_id")
    private UUID transportDocumentID;

    @Column("collection_datetime")
    private LocalDateTime collectionDatetime;

    @Column("delivery_datetime")
    private LocalDateTime deliveryDatetime;

    @Column("carrier_code")
    @Size(max = 10)
    private String carrierCode;

    @Column("export_reference_number")
    private Integer exportReferenceNumber;

    @Column("shipment_on_board_date")
    private LocalDateTime shipmentOnBoardDate;

    @Column("pre_carrier_mode_of_transport")
    @Size(max = 3)
    private String preCarrierModeOfTransport;

    @Column("shipment_equipment_quantity")
    private Integer shipmentEquipmentQuantity;

    @Column("svc_contract")
    @Size(max = 30)
    private String svcContract;

    @Column("declared_value")
    private Integer declaredValue;

    @Column("declared_value_currency")
    @Size(max = 3)
    private String declaredValueCurrency;
}
