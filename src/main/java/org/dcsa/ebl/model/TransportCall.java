package org.dcsa.ebl.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dcsa.core.model.AuditBase;
import org.dcsa.core.model.GetId;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import javax.persistence.Id;
import javax.validation.constraints.Size;
import java.util.UUID;

@Table("transport_call")
@Data
@EqualsAndHashCode(callSuper = true)
public class TransportCall extends AuditBase implements GetId<UUID> {
    @Id
    private UUID id;

    @Column("transport_call_sequence_number")
    private Integer transportCallSequenceNumber;

    @Column("facility_code")
    @Size(max = 11)
    private String facilityCode;

    @Column("facility_type_code")
    @Size(max = 4)
    private String facilityTypeCode;

    @Column("other_facility")
    @Size(max = 50)
    private String otherFacility;

    @Column("location_id")
    private UUID locationID;
}
