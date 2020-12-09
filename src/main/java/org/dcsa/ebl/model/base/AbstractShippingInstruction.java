package org.dcsa.ebl.model.base;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dcsa.core.model.AuditBase;
import org.dcsa.core.model.GetId;
import org.dcsa.ebl.model.enums.TransportDocumentTypeCode;
import org.springframework.data.relational.core.mapping.Column;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@NoArgsConstructor
public class AbstractShippingInstruction extends AuditBase implements GetId<UUID> {

    @JsonProperty("shippingInstructionID")
    private UUID id;

    @Column("number_of_copies")
    private Integer numberOfCopies;

    @Column("number_of_originals")
    private Integer numberOfOriginals;

    @Column("is_electronic")
    private Boolean isElectronic;

    @Column("is_part_load")
    private Boolean isPartLoad;

    @Column("transport_document_type")
    private TransportDocumentTypeCode transportDocumentType;

    public void setTransportDocumentType(String transportDocumentType) {
        this.transportDocumentType = TransportDocumentTypeCode.valueOf(transportDocumentType);
    }

    public void setTransportDocumentType(TransportDocumentTypeCode transportDocumentType) {
        this.transportDocumentType = transportDocumentType;
    }

    @Column("callback_url")
    @NotNull
    private String callbackUrl;

}
