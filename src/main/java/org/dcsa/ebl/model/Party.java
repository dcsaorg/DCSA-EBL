package org.dcsa.ebl.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.dcsa.core.model.GetId;

import javax.validation.constraints.Size;
import java.util.UUID;

@Data
@NoArgsConstructor
public class Party implements GetId<UUID> {
    private UUID id;

    @Size(max = 100)
    private String party_name;

    @Size(max = 20)
    private String tax_reference;

    @Size(max = 500)
    private String public_key;
}
