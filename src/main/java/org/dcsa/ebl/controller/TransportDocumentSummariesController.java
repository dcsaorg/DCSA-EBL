package org.dcsa.ebl.controller;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.controller.AsymmetricQueryController;
import org.dcsa.core.events.model.TransportDocument;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.core.extendedrequest.ExtendedParameters;
import org.dcsa.core.extendedrequest.ExtendedRequest;
import org.dcsa.core.validator.EnumSubset;
import org.dcsa.ebl.extendedrequest.ExtendedTransportDocumentSummaryRequest;
import org.dcsa.ebl.model.TransportDocumentSummary;
import org.dcsa.ebl.service.TransportDocumentService;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping(
    value = "transport-document-summaries",
    produces = {MediaType.APPLICATION_JSON_VALUE})
public class TransportDocumentSummariesController
    extends AsymmetricQueryController<
        TransportDocumentService, TransportDocument, TransportDocumentSummary, UUID> {

  private final ExtendedParameters extendedParameters;

  private final TransportDocumentService transportDocumentService;

  @GetMapping
  public Mono<List<TransportDocumentSummary>> getBookingConfirmationSummaries(
      @RequestParam(value = "carrierBookingReference", required = false)
          String carrierBookingReference,
      @RequestParam(value = "documentStatus", required = false)
          @EnumSubset(anyOf = ShipmentEventTypeCode.EBL_DOCUMENT_STATUSES)
          ShipmentEventTypeCode documentStatus,
      ServerHttpResponse response,
      ServerHttpRequest request) {

    return super.findAll(response, request).collectList();
  }

  @Override
  protected ExtendedRequest<TransportDocument> newExtendedRequest() {
    return new ExtendedTransportDocumentSummaryRequest(
        extendedParameters, r2dbcDialect, TransportDocument.class);
  }

  @Override
  protected TransportDocumentService getService() {
    return transportDocumentService;
  }
}
