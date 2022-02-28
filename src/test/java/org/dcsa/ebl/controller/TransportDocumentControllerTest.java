package org.dcsa.ebl.controller;

import org.dcsa.core.events.edocumentation.model.transferobject.BookingTO;
import org.dcsa.core.events.edocumentation.model.transferobject.CarrierClauseTO;
import org.dcsa.core.events.edocumentation.model.transferobject.ChargeTO;
import org.dcsa.core.events.edocumentation.model.transferobject.ShipmentTO;
import org.dcsa.core.events.model.Address;
import org.dcsa.core.events.model.enums.PaymentTerm;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.core.events.model.transferobjects.LocationTO;
import org.dcsa.core.events.model.transferobjects.ShippingInstructionTO;
import org.dcsa.core.exception.ConcreteRequestErrorMessageException;
import org.dcsa.core.exception.handler.GlobalExceptionHandler;
import org.dcsa.core.security.SecurityConfig;
import org.dcsa.ebl.model.transferobjects.ApproveTransportDocumentRequestTO;
import org.dcsa.ebl.model.transferobjects.TransportDocumentTO;
import org.dcsa.ebl.service.TransportDocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@DisplayName("Tests for TransportDocumentController")
@ActiveProfiles("test")
@WebFluxTest(controllers = {TransportDocumentController.class})
@Import(value = {GlobalExceptionHandler.class, SecurityConfig.class})
class TransportDocumentControllerTest {

  @Autowired WebTestClient webTestClient;

  @MockBean TransportDocumentService transportDocumentService;

  private final String TRANSPORT_DOCUMENT_ENDPOINT = "/transport-documents";

  TransportDocumentTO transportDocumentTO;
  TransportDocumentTO approvedTransportDocument;
  ApproveTransportDocumentRequestTO validTransportDocumentRequestTO;
  ApproveTransportDocumentRequestTO invalidTransportDocumentRequestTO;

  @BeforeEach
  private void init() {
    transportDocumentTO = new TransportDocumentTO();
    transportDocumentTO.setTransportDocumentReference("TransportDocumentReference1");

    Address address = new Address();
    address.setCity("Amsterdam");
    address.setCountry("Netherlands");
    address.setStreet("Strawinskylaan");
    address.setPostalCode("1077ZX");
    address.setStreetNumber("4117");
    address.setFloor("6");
    address.setStateRegion("Noord-Holland");

    LocationTO locationTO = new LocationTO();
    locationTO.setLocationName("DCSA Headquarters");
    locationTO.setAddress(address);
    locationTO.setId("1");

    ChargeTO chargeTO = new ChargeTO();
    chargeTO.setChargeTypeCode("chargeTypeCode");
    chargeTO.setCalculationBasis("CalculationBasics");
    chargeTO.setCurrencyAmount(100.0);
    chargeTO.setCurrencyCode("EUR");
    chargeTO.setQuantity(1.0);
    chargeTO.setUnitPrice(100.0);
    chargeTO.setPaymentTermCode(PaymentTerm.COL);

    CarrierClauseTO carrierClauseTO = new CarrierClauseTO();
    carrierClauseTO.setClauseContent("CarrierClause");

    ShippingInstructionTO shippingInstructionTO = new ShippingInstructionTO();
    shippingInstructionTO.setIsShippedOnboardType(true);
    shippingInstructionTO.setIsElectronic(true);
    shippingInstructionTO.setIsToOrder(true);
    shippingInstructionTO.setShippingInstructionID(UUID.randomUUID().toString());
    shippingInstructionTO.setDocumentStatus(ShipmentEventTypeCode.RECE);
    shippingInstructionTO.setPlaceOfIssueID(locationTO.getId());
    shippingInstructionTO.setAreChargesDisplayedOnCopies(true);

    ShipmentTO shipmentTO = new ShipmentTO();
    BookingTO bookingTO = new BookingTO();
    bookingTO.setDocumentStatus(ShipmentEventTypeCode.CMPL);
    shipmentTO.setBooking(bookingTO);
    shippingInstructionTO.setShipments(List.of(shipmentTO));

    transportDocumentTO = new TransportDocumentTO();
    transportDocumentTO.setCharges(List.of(chargeTO));
    transportDocumentTO.setPlaceOfIssue(locationTO);
    transportDocumentTO.setCarrierClauses(List.of(carrierClauseTO));
    transportDocumentTO.setShippingInstruction(shippingInstructionTO);
    transportDocumentTO.setTransportDocumentReference("TRDocReference1");

    // request body for valid & invalid approval request body
    validTransportDocumentRequestTO = new ApproveTransportDocumentRequestTO();
    validTransportDocumentRequestTO.setDocumentStatus(ShipmentEventTypeCode.APPR);
    invalidTransportDocumentRequestTO = new ApproveTransportDocumentRequestTO();
    invalidTransportDocumentRequestTO.setDocumentStatus(ShipmentEventTypeCode.RECE);

    // response approved TD
    ShippingInstructionTO approvedShippingInstructionTO = new ShippingInstructionTO();
    approvedShippingInstructionTO.setIsShippedOnboardType(true);
    approvedShippingInstructionTO.setIsElectronic(true);
    approvedShippingInstructionTO.setIsToOrder(true);
    approvedShippingInstructionTO.setShippingInstructionID(UUID.randomUUID().toString());
    approvedShippingInstructionTO.setDocumentStatus(ShipmentEventTypeCode.APPR);
    approvedShippingInstructionTO.setPlaceOfIssueID(locationTO.getId());
    approvedShippingInstructionTO.setAreChargesDisplayedOnCopies(true);
    approvedShippingInstructionTO.setShipments(List.of(shipmentTO));

    approvedTransportDocument = new TransportDocumentTO();
    approvedTransportDocument.setCharges(List.of(chargeTO));
    approvedTransportDocument.setPlaceOfIssue(locationTO);
    approvedTransportDocument.setCarrierClauses(List.of(carrierClauseTO));
    approvedTransportDocument.setShippingInstruction(approvedShippingInstructionTO);
    approvedTransportDocument.setTransportDocumentReference("approvedTRDocReference");
    approvedTransportDocument.setTransportDocumentReference("TransportDocumentReference1");

  }

  @Test
  @DisplayName("Get transport document with valid reference should return transport document.")
  void testGetTransportDocumentByReference() {
    when(transportDocumentService.findByTransportDocumentReference(eq("TRDocReference1")))
        .thenReturn(Mono.just(transportDocumentTO));

    webTestClient
        .get()
        .uri(
            uriBuilder ->
                uriBuilder.path(TRANSPORT_DOCUMENT_ENDPOINT).pathSegment("TRDocReference1").build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody()
        .consumeWith(System.out::println)
        .jsonPath("$.transportDocumentReference")
        .hasJsonPath();
  }

  @Test
  @DisplayName("Get transport document with unknown reference should return not found")
  void testGetTransportDocumentNotFound() {
    when(transportDocumentService.findByTransportDocumentReference(any())).thenReturn(Mono.empty());

    webTestClient
        .get()
        .uri(
            uriBuilder ->
                uriBuilder.path(TRANSPORT_DOCUMENT_ENDPOINT).pathSegment("unknown").build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isNotFound()
        .expectBody();
  }

  @Test
  @DisplayName("Get transport document with invalid reference should return bad request")
  void testGetTransportDocumentBadRequest() {
    webTestClient
        .get()
        .uri(
            uriBuilder ->
                uriBuilder
                    .path(TRANSPORT_DOCUMENT_ENDPOINT)
                    .pathSegment("1234567890123456789013434324")
                    .build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody();
  }

  @Test
  @DisplayName("Get transport document with valid reference should return transport document.")
  void testGetTransportDocumentByReferenceValidateSchema() {
    when(transportDocumentService.findByTransportDocumentReference(eq("TRDocReference1")))
        .thenReturn(Mono.just(transportDocumentTO));

    webTestClient
        .get()
        .uri(
            uriBuilder ->
                uriBuilder.path(TRANSPORT_DOCUMENT_ENDPOINT).pathSegment("TRDocReference1").build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody();
  }

  @Test
  @DisplayName("Approve at transport document with valid reference should return transport document with SI & booking " +
    "document statuses set to APPR & CMPL respectively")
  void testApproveTransportDocumentByReference() {

    when(transportDocumentService.ApproveTransportDocument("TRDocReference1"))
      .thenReturn(Mono.just(approvedTransportDocument));

    webTestClient
      .put()
      .uri(
        uriBuilder ->
          uriBuilder.path(TRANSPORT_DOCUMENT_ENDPOINT).pathSegment("TRDocReference1").build())
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(validTransportDocumentRequestTO))
      .exchange()
      .expectStatus()
      .isOk()
      .expectBody()
      .consumeWith(System.out::println)
      .jsonPath("$.transportDocumentReference").hasJsonPath()
      .jsonPath("$.shippingInstruction.documentStatus").hasJsonPath()
      .jsonPath("$.shippingInstruction.shipments[0].booking.documentStatus").hasJsonPath();

  }


  @Test
  @DisplayName("Approve transport document with invalid request body should return bad request")
  void testApproveTransportDocumentWithInvalidBodyRequest() {

      webTestClient
        .put()
        .uri(
          uriBuilder ->
            uriBuilder.path(TRANSPORT_DOCUMENT_ENDPOINT).pathSegment("TRDocReference1").build())
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(invalidTransportDocumentRequestTO))
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  @DisplayName("Approving a transport document that has a SI with invalid status document status should return bad request")
  void testApproveTransportDocumentThatHasShippingInstructionInvalidDocumentStatus() {

    when(transportDocumentService.ApproveTransportDocument("TRDocReference1"))
      .thenReturn(
        Mono.error(
          ConcreteRequestErrorMessageException.notFound(
            "Cannot Approve Transport Document with Shipping Instruction that is not in status PENA")));

    webTestClient
        .put()
        .uri(
          uriBuilder ->
            uriBuilder.path(TRANSPORT_DOCUMENT_ENDPOINT).pathSegment("TRDocReference1").build())
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(validTransportDocumentRequestTO))
        .exchange()
        .expectStatus()
        .isBadRequest();
  }


  @Test
  @DisplayName("Approving a transport document that has a SI with no shipments should return bad request")
  void testApproveTransportDocumentThatHasShippingInstructionWithNoShipments() {

    when(transportDocumentService.ApproveTransportDocument("TRDocReference1"))
      .thenReturn(
        Mono.error(
          ConcreteRequestErrorMessageException.notFound(
            "No shipments found for Shipping instruction of transport document reference: " + "{transport document ID}")));

    webTestClient
      .put()
      .uri(
        uriBuilder ->
          uriBuilder.path(TRANSPORT_DOCUMENT_ENDPOINT).pathSegment("TRDocReference1").build())
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(validTransportDocumentRequestTO))
      .exchange()
      .expectStatus()
      .isBadRequest();
  }
}
