package org.dcsa.ebl.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dcsa.ebl.model.base.AbstractCharge;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("charges")
@Data
@EqualsAndHashCode(callSuper = true)
public class Charge extends AbstractCharge {

    @Column("shipment_id")
    private UUID shipmentID;

}
