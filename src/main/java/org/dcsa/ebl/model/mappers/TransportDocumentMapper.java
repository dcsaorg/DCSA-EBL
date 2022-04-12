package org.dcsa.ebl.model.mappers;

import org.dcsa.core.events.model.TransportDocument;
import org.dcsa.ebl.model.TransportDocumentSummary;
import org.dcsa.ebl.model.transferobjects.TransportDocumentTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransportDocumentMapper {

    @Mapping(source = "issueDate", target = "issueDate")
    @Mapping(source = "shippedOnBoardDate", target = "shippedOnboardDate")
    @Mapping(source = "transportDocumentRequestCreatedDateTime", target = "transportDocumentCreatedDateTime")
    @Mapping(source = "transportDocumentRequestUpdatedDateTime", target = "transportDocumentUpdatedDateTime")
    TransportDocumentSummary transportDocumentToTransportDocumentSummary(TransportDocument transportDocument);


  @Mapping(source = "placeOfIssue", target = "placeOfIssue", ignore = true)
  TransportDocumentTO transportDocumentToDTO(TransportDocument transportDocument);

  @Mapping(source = "placeOfIssue", target = "placeOfIssue", ignore = true)
  TransportDocument dtoToTransportDocument(TransportDocumentTO transportDocumentTO);
}
