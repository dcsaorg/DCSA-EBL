package org.dcsa.ebl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.dcsa.core.model.GetId;
import org.dcsa.ebl.model.enums.PartyFunction;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.UUID;

@Table("displayed_address")
@Data
public class DisplayedAddress implements GetId<UUID> {

    @Id
    @JsonIgnore
    private UUID id;  /* TODO: Remove */

    @Column("party_id")
    private UUID partyID;

    @Column("party_function")
    private PartyFunction partyFunction;

    @Column("shipping_instruction_id")
    private UUID shippingInstructionID;

    @Column("shipment_id")
    private UUID shipmentID;

    @Column("address_line")
    @Size(max=250)
    private String addressLine;

    @NotNull
    @Column("address_line_number")
    private Integer addressLineNumber;
}
