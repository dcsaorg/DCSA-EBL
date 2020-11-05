package org.dcsa.ebl.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dcsa.core.model.GetId;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Table("shipping_instruction")
@Data
@NoArgsConstructor
public class ShippingInstruction extends BaseClass implements GetId<UUID> {

    @Id
    @JsonProperty("shippingInstructionID")
    private UUID id;

    @NotNull
    @Size(max = 50)
    private String transportReference;

    @NotNull
    private LocalDate dateOfIssue;

    @Size(max = 20)
    private String taxReference;

    private Integer verifiedGrossMass;



    @Column("call_back_url")
    private String callBackUrl;

    private Shipment shipment;

    private List<CargoItem> cargoItems;

    private List<Equipment> equipments;
}


