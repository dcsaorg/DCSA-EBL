package org.dcsa.ebl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dcsa.core.model.AuditBase;
import org.dcsa.core.model.GetId;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

import javax.validation.constraints.Size;
import java.util.UUID;

@NoArgsConstructor
@Data
public class Seal extends AuditBase implements GetId<UUID> {

    @Id
    @JsonIgnore
    private UUID id;

    @JsonIgnore
    @Column("shipment_equipment_id")
    private UUID shipmentEquipmentID;

    @Column("seal_number")
    @Size(max = 15)
    private String sealNumber;

    @Column("seal_source")
    @Size(max = 5)
    private String sealSource;

    @Column("seal_type")
    @Size(max = 5)
    private String sealType;
}
