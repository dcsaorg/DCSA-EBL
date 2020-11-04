package org.dcsa.ebl.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

@Table("shipping_instruction")
@Data
@NoArgsConstructor
public class ShippingInstruction extends BaseClass {

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
}
