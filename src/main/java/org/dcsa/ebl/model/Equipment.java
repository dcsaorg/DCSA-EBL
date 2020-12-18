package org.dcsa.ebl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.dcsa.core.model.GetId;
import org.dcsa.ebl.model.base.AbstractEquipment;
import org.springframework.data.relational.core.mapping.Table;

@Table("equipment")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Equipment extends AbstractEquipment implements GetId<String> {

    @JsonIgnore
    @Override
    public String getId() {
        return this.getEquipmentReference();
    }
}
