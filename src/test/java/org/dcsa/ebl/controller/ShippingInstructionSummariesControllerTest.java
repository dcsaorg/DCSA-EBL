package org.dcsa.ebl.controller;

import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.core.exception.handler.GlobalExceptionHandler;
import org.dcsa.core.extendedrequest.ExtendedParameters;
import org.dcsa.core.security.SecurityConfig;
import org.dcsa.ebl.model.transferobjects.ShippingInstructionSummaryTO;
import org.dcsa.ebl.service.ShippingInstructionSummariesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.r2dbc.dialect.R2dbcDialect;
import org.springframework.r2dbc.core.binding.BindMarkersFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Tests for ShippingInstructionSummariesController")
@ActiveProfiles("test")
@WebFluxTest(controllers = {ShippingInstructionSummariesController.class})
@Import(value = {GlobalExceptionHandler.class, SecurityConfig.class})
public class ShippingInstructionSummariesControllerTest {

  @Autowired
  private WebTestClient webTestClient;

  @MockBean
  private ExtendedParameters extendedParameters;

  @MockBean
  private R2dbcDialect r2dbcDialect;

  @MockBean
  private ShippingInstructionSummariesService service;

  @BeforeEach
  public void init() {
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
  }

  @Test
  @DisplayName("emptyRequestShouldReturnValues")
  void emptyRequestShouldReturnValues() {
    ShippingInstructionSummaryTO expectedResult = buildShippingInstructionSummaryTO();

    when(service.findShippingInstructionSummaries(any()))
        .thenReturn(Flux.just(expectedResult));

    verifyShippingInstructionSummaryTO(webTestClient
      .get()
      .uri("/shipping-instructions-summaries")
      .exchange()
      .expectStatus().isOk()
      .expectBody(),
      expectedResult)
    ;

    verify(service).findShippingInstructionSummaries(any());
  }

  @ParameterizedTest
  @DisplayName("validDocumentStatusShouldReturnValue")
  @MethodSource("validEblDocumentStatuses")
  void validDocumentStatusShouldReturnValue(String documentStatus) {
    ShippingInstructionSummaryTO expectedResult = buildShippingInstructionSummaryTO();

    when(service.findShippingInstructionSummaries(any()))
      .thenReturn(Flux.just(expectedResult));

    verifyShippingInstructionSummaryTO(webTestClient
        .get()
        .uri(uriBuilder -> uriBuilder
          .path("/shipping-instructions-summaries")
          .queryParam("documentStatus", documentStatus)
          .build()
        )
        .exchange()
        .expectStatus().isOk()
        .expectBody(),
      expectedResult)
    ;

    verify(service).findShippingInstructionSummaries(any());
  }

  @ParameterizedTest
  @MethodSource("invalidEblDocumentStatuses")
  @DisplayName("invalidDocumentStatusShouldReturn401")
  void invalidDocumentStatusShouldReturn401(String documentStatus) {
    webTestClient
        .get()
        .uri(uriBuilder -> uriBuilder
            .path("/shipping-instructions-summaries")
            .queryParam("documentStatus", documentStatus)
            .build()
          )
        .exchange()
        .expectStatus().is4xxClientError();
  }

  private ShippingInstructionSummaryTO buildShippingInstructionSummaryTO() {
    return ShippingInstructionSummaryTO.builder()
      .shippingInstructionID(UUID.randomUUID().toString())
      .documentStatus(ShipmentEventTypeCode.RECE)
      .carrierBookingReferences(List.of("bca68f1d3b804ff88aaa1e43055432f7", "832deb4bd4ea4b728430b857c59bd057"))
      .build();
  }

  private void verifyShippingInstructionSummaryTO(WebTestClient.BodyContentSpec result, ShippingInstructionSummaryTO expectedResult) {
    result
      .jsonPath("$.[0].shippingInstructionID").isEqualTo(expectedResult.getShippingInstructionID())
      .jsonPath("$.[0].documentStatus").isEqualTo(expectedResult.getDocumentStatus().name())
      .jsonPath("$.[0].carrierBookingReferences").exists()
      ;
  }

  private static Stream<String> validEblDocumentStatuses() {
    return Stream.of(ShipmentEventTypeCode.EBL_DOCUMENT_STATUSES.split(","));
  }

  private static Stream<String> invalidEblDocumentStatuses() {
    Set<String> validValues = Set.of(ShipmentEventTypeCode.EBL_DOCUMENT_STATUSES.split(","));
    return Arrays.stream(ShipmentEventTypeCode.values())
      .filter(documentStatus -> !validValues.contains(documentStatus.name()))
      .map(documentStatus -> documentStatus.name());
  }
}
