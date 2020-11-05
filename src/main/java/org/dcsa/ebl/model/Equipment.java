package org.dcsa.ebl.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.dcsa.core.model.AuditBase;
import org.dcsa.core.model.GetId;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.Size;

@Table("equipment")
@Data
@NoArgsConstructor
public class Equipment extends AuditBase implements GetId<String> {
    @Id
    @Column("equipment_reference")
    @Size(max = 15)
    private String equipmentReference;

    public String getId() {
        return equipmentReference;
    }

    public void setId(String id) {
        equipmentReference = id;
    }

    @Column("iso_equipment_code")
    @Size(max = 4)
    private String isoEquipmentCode;

    private Integer tareWeight;

    @Column("weight_unit")
    @Size(max = 50)
    private String weightUnit;
}
