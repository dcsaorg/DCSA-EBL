package org.dcsa.ebl.model.transferobjects;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.dcsa.ebl.model.Charge;
import org.dcsa.ebl.model.TransportDocument;
import org.springframework.data.relational.core.mapping.Table;

import java.util.List;

@Table("transport_document")
@Data
@NoArgsConstructor
public class TransportDocumentTO extends TransportDocument {
    private List<Charge> charges;
}
