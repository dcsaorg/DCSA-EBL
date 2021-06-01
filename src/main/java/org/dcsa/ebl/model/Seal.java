package org.dcsa.ebl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.dcsa.core.model.AuditBase;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.Size;
import java.util.UUID;

@NoArgsConstructor
@Data
@Table("seal")
@EqualsAndHashCode(callSuper = true)
public class Seal extends AuditBase {

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
