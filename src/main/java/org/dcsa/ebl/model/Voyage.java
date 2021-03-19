package org.dcsa.ebl.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.dcsa.core.model.GetId;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.Size;
import java.util.UUID;

@Data
@NoArgsConstructor
@Table("voyage")
public class Voyage implements GetId<UUID> {
    @Id
    private UUID id;

    @Column("carrier_voyage_number")
    @Size(max = 50)
    private String carrierVoyageNumber;

    @Column("service_id")
    private UUID serviceID;
}
