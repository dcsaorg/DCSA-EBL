package org.dcsa.ebl.model.mappers;

import org.dcsa.core.events.model.TransportDocument;
import org.dcsa.ebl.model.TransportDocumentSummary;
import org.dcsa.ebl.model.transferobjects.TransportDocumentTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransportDocumentMapper {
//    @Mapping(source = "placeOfIssueID", target = "placeOfIssue.id", ignore = true)
    TransportDocumentTO transportDocumentToDTO(TransportDocument transportDocument);

    TransportDocument dtoToTransportDocument(TransportDocumentTO transportDocumentTO);

    @Mapping(source = "dateOfIssue", target = "issueDate")
    @Mapping(source = "issuer", target = "issuerCode")
    @Mapping(source = "onboardDate", target = "shippedOnboardDate")
    TransportDocumentSummary transportDocumentToTransportDocumentSummary(TransportDocument transportDocument);
}
