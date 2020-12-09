package org.dcsa.ebl.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.dcsa.core.model.AuditBase;
import org.dcsa.core.model.GetId;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Data
public class EBLEndorsementChain extends AuditBase implements GetId<UUID> {

    @Id
    private UUID id;  /* TODO: Remove */

    @Column("transport_document_id")
    @NotNull
    private UUID transportDocumentID;

    @Column("title_holder")
    @NotNull
    private UUID titleHolder;

    @Size(max = 500)
    @NotNull
    private String signature;

    @Column("endorsement_datetime")
    @NotNull
    private LocalDateTime endorsementDateTime;

    @NotNull
    private UUID endorsee;
}
