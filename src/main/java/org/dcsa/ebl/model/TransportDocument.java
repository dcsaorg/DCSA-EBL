package org.dcsa.ebl.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dcsa.core.model.AuditBase;
import org.dcsa.core.model.GetId;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("transport_document")
@Data
@NoArgsConstructor
public class TransportDocument extends AuditBase implements GetId<UUID> {

    @Id
    @JsonProperty("transportDocumentID")
    private UUID id;
}
