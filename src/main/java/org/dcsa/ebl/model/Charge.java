package org.dcsa.ebl.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dcsa.core.model.GetId;
import org.dcsa.ebl.model.base.AbstractCharge;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("charges")
@Data
@EqualsAndHashCode(callSuper = true)
public class Charge extends AbstractCharge implements GetId<UUID> {
    @Id
    private UUID id;

    @Column("freight_payable_at")
    private UUID freightPayableAt;
}
