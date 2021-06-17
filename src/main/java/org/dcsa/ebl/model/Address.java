package org.dcsa.ebl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.Size;
import java.util.UUID;

@Table("address")
@Data
@NoArgsConstructor
public class Address {

    @Id
    @JsonIgnore
    private UUID id;

    @Size(max = 100)
    private String name;

    @Size(max = 100)
    private String street;

    @Size(max = 50)
    @Column("street_number")
    private String streetNumber;

    @Size(max = 50)
    private String floor;

    @Column("postal_code")
    @Size(max = 10)
    private String postalCode;

    @Size(max = 65)
    private String city;

    @Column("state_region")
    @Size(max = 75)
    private String stateRegion;

    @Size(max = 75)
    private String country;

}
