package org.dcsa.ebl.controller;

import org.dcsa.skernel.model.enums.CarrierCodeListProvider;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.core.exception.handler.GlobalExceptionHandler;
import org.dcsa.core.extendedrequest.ExtendedParameters;
import org.dcsa.core.security.SecurityConfig;
import org.dcsa.ebl.model.TransportDocumentSummary;
import org.dcsa.ebl.model.mappers.TransportDocumentMapper;
import org.dcsa.ebl.repository.ShippingInstructionRepository;
import org.dcsa.ebl.service.TransportDocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.r2dbc.dialect.R2dbcDialect;
import org.springframework.http.MediaType;
import org.springframework.r2dbc.core.binding.BindMarkersFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DisplayName("Tests for TransportDocumentSummariesController")
@ActiveProfiles("test")
@WebFluxTest(controllers = {TransportDocumentSummariesController.class})
@Import(value = {GlobalExceptionHandler.class, SecurityConfig.class})
class TransportDocumentSummariesControllerTest {

  @Autowired WebTestClient webTestClient;

  @MockBean private ExtendedParameters extendedParameters;
  @MockBean private R2dbcDialect r2dbcDialect;
  @MockBean TransportDocumentService transportDocumentService;
  @MockBean ShippingInstructionRepository shippingInstructionRepository;

  @Spy
  TransportDocumentMapper TransportDocumentMapper =
      Mappers.getMapper(TransportDocumentMapper.class);

  private final String TRANSPORT_DOCUMENT_SUMMARIES_ENDPOINT = "/transport-document-summaries";

  private TransportDocumentSummary transportDocumentSummary;

  @BeforeEach
  void init() {
    when(extendedParameters.getSortParameterName()).thenReturn("sort");
    when(extendedParameters.getPaginationPageSizeName()).thenReturn("limit");
    when(extendedParameters.getPaginationCursorName()).thenReturn("cursor");
    when(extendedParameters.getIndexCursorName()).thenReturn("|Offset|");
    when(extendedParameters.getEnumSplit()).thenReturn(",");
    when(extendedParameters.getQueryParameterAttributeSeparator()).thenReturn(",");
    when(extendedParameters.getPaginationCurrentPageName()).thenReturn("Current-Page");
    when(extendedParameters.getPaginationFirstPageName()).thenReturn("First-Page");
    when(extendedParameters.getPaginationPreviousPageName()).thenReturn("Last-Page");
    when(extendedParameters.getPaginationNextPageName()).thenReturn("Next-Page");
    when(extendedParameters.getPaginationLastPageName()).thenReturn("Last-Page");

    when(r2dbcDialect.getBindMarkersFactory()).thenReturn(BindMarkersFactory.anonymous("?"));

    OffsetDateTime now = OffsetDateTime.now();
    transportDocumentSummary = new TransportDocumentSummary();
    transportDocumentSummary.setShippingInstructionReference(UUID.randomUUID().toString());
    transportDocumentSummary.setDocumentStatus(ShipmentEventTypeCode.RECE);
    transportDocumentSummary.setTransportDocumentCreatedDateTime(now);
    transportDocumentSummary.setTransportDocumentUpdatedDateTime(now);
    transportDocumentSummary.setCarrierCode("x".repeat(3));
    transportDocumentSummary.setCarrierCodeListProvider(CarrierCodeListProvider.SMDG);
    transportDocumentSummary.setIssueDate(LocalDate.now());
    transportDocumentSummary.setIssueDate(LocalDate.now());
    transportDocumentSummary.setShippedOnboardDate(LocalDate.now());
    transportDocumentSummary.setReceivedForShipmentDate(LocalDate.now());
    transportDocumentSummary.setNumberOfRiderPages(10);
    transportDocumentSummary.setCarrierBookingReferences(List.of("CarrierBookingReference"));
    transportDocumentSummary.setTransportDocumentReference("TDref");
  }

  @Test
  @DisplayName(
      "GET transport document summaries should return 200 and valid transport document summary json schema.")
  void getTransportDocumentSummaries() {

    // mock service method call
    when(transportDocumentService.findAllExtended(any()))
        .thenReturn(Flux.just(transportDocumentSummary));

    WebTestClient.ResponseSpec exchange =
        webTestClient
            .get()
            .uri(TRANSPORT_DOCUMENT_SUMMARIES_ENDPOINT)
            .accept(MediaType.APPLICATION_JSON)
            .exchange();

    checkStatus200.andThen(checkTransportDocumentSummariesResponseTOJsonSchema).apply(exchange);
  }

  @Test
  @DisplayName("GET transport document summaries by document status should return 200 and valid transport document summary json schema.")
  void getTransportDocumentSummariesByDocumentStatus() {

    when(transportDocumentService.findAllExtended(any()))
        .thenReturn(Flux.just(transportDocumentSummary));

    WebTestClient.ResponseSpec exchange =
        webTestClient
            .get()
            .uri(
                uriBuilder ->
                    uriBuilder
                        .path(TRANSPORT_DOCUMENT_SUMMARIES_ENDPOINT)
                        .queryParam("documentStatus", ShipmentEventTypeCode.RECE)
                        .build())
            .accept(MediaType.APPLICATION_JSON)
            .exchange();

    checkStatus200.andThen(checkDocumentStatusFilter).apply(exchange);
    checkTransportDocumentSummariesResponseTOJsonSchema.apply(exchange);
  }

  @Test
  @DisplayName("GET transport document summaries by carrier booking references should return 200 and valid transport document summary json schema.")
  void getTransportDocumentSummariesByCarrierBooingReference() {

    when(transportDocumentService.findAllExtended(any()))
        .thenReturn(Flux.just(transportDocumentSummary));

    WebTestClient.ResponseSpec exchange =
        webTestClient
            .get()
            .uri(
                uriBuilder ->
                    uriBuilder
                        .path(TRANSPORT_DOCUMENT_SUMMARIES_ENDPOINT)
                        .queryParam("carrierBookingReference", "CarrierBookingReferenc")
                        .build())
            .accept(MediaType.APPLICATION_JSON)
            .exchange();

    checkStatus200.andThen(checkTransportDocumentSummariesResponseTOJsonSchema).apply(exchange);
  }

  private final Function<WebTestClient.ResponseSpec, WebTestClient.ResponseSpec> checkStatus200 =
      (exchange) -> exchange.expectStatus().isOk();

  private final Function<WebTestClient.ResponseSpec, WebTestClient.BodyContentSpec> checkDocumentStatusFilter =
      (exchange) -> exchange
        .expectBody()
        .consumeWith(System.out::println)
        .jsonPath("$.[0].documentStatus")
        .isEqualTo(ShipmentEventTypeCode.RECE.name());

  private final Function<WebTestClient.ResponseSpec, WebTestClient.BodyContentSpec>
    checkTransportDocumentSummariesResponseTOJsonSchema =
          (exchange) ->
              exchange
                  .expectBody()
                  .consumeWith(System.out::println)
                  .jsonPath("$.[0].shippingInstructionReference")
                  .hasJsonPath()
                  .jsonPath("$.[0].documentStatus")
                  .hasJsonPath()
                  .jsonPath("$.[0].transportDocumentCreatedDateTime")
                  .hasJsonPath()
                  .jsonPath("$.[0].transportDocumentUpdatedDateTime")
                  .hasJsonPath()
                  .jsonPath("$.[0].issueDate")
                  .hasJsonPath()
                  .jsonPath("$.[0].shippedOnboardDate")
                  .hasJsonPath()
                  .jsonPath("$.[0].receivedForShipmentDate")
                  .hasJsonPath()
                  .jsonPath("$.[0].carrierCode")
                  .hasJsonPath()
                  .jsonPath("$.[0].carrierCodeListProvider")
                  .hasJsonPath()
                  .jsonPath("$.[0].numberOfRiderPages")
                  .hasJsonPath()
                  .jsonPath("$.[0].carrierBookingReferences")
                  .hasJsonPath();
}
