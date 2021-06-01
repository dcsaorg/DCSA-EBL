package org.dcsa.ebl.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dcsa.ebl.model.base.AbstractShippingInstruction;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.NotNull;
import java.util.UUID;


@Data
@EqualsAndHashCode(callSuper = true)
@Table("shipping_instruction")
public class ShippingInstruction extends AbstractShippingInstruction {

    @NotNull
    @Column("invoice_payable_at")
    private String freightPayableAt;
}
