package org.dcsa.ebl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.dcsa.core.model.GetId;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.Email;
import java.util.UUID;

@Table("party_contact_details")
@Data
public class PartyContactDetails implements GetId<UUID> {

    @Id
    @JsonIgnore
    private UUID id;

    private String name;

    private String phone;

    @Email
    private String email;

    private String fax;
}
