package org.dcsa.ebl.controller;

import org.dcsa.core.events.edocumentation.model.transferobject.*;
import org.dcsa.core.events.model.Reference;
import org.dcsa.core.events.model.enums.*;
import org.dcsa.core.events.model.transferobjects.*;
import org.dcsa.core.exception.ConcreteRequestErrorMessageException;
import org.dcsa.core.exception.handler.GlobalExceptionHandler;
import org.dcsa.core.security.SecurityConfig;
import org.dcsa.ebl.model.transferobjects.ApproveTransportDocumentRequestTO;
import org.dcsa.ebl.model.transferobjects.TransportDocumentRefStatusTO;
import org.dcsa.ebl.model.transferobjects.TransportDocumentTO;
import org.dcsa.ebl.service.TransportDocumentService;
import org.dcsa.skernel.model.Address;
import org.dcsa.skernel.model.PartyContactDetails;
import org.dcsa.skernel.model.enums.CarrierCodeListProvider;
import org.dcsa.skernel.model.transferobjects.LocationTO;
import org.dcsa.skernel.model.transferobjects.PartyContactDetailsTO;
import org.dcsa.skernel.model.transferobjects.PartyTO;
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

import java.time.OffsetDateTime;
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
  TransportDocumentRefStatusTO approvedTransportDocument;
  ApproveTransportDocumentRequestTO validTransportDocumentRequestTO;
  ApproveTransportDocumentRequestTO invalidTransportDocumentRequestTO;
  Address address;
  Reference reference;

  @BeforeEach
  void init() {
    initEntities();
    initTO();
  }

  void initEntities() {

    address = new Address();
    address.setCity("Amsterdam");
    address.setCountry("Netherlands");
    address.setStreet("Strawinskylaan");
    address.setPostalCode("1077ZX");
    address.setStreetNumber("4117");
    address.setFloor("6");
    address.setStateRegion("Noord-Holland");

    reference = new Reference();
    reference.setReferenceValue("test");
    reference.setReferenceType(ReferenceTypeCode.FF);

  }

  private void initTO() {

    ReferenceTO referenceTO = new ReferenceTO();
    referenceTO.setReferenceType(reference.getReferenceType());
    referenceTO.setReferenceValue(reference.getReferenceValue());

    CargoLineItemTO cargoLineItemTO = new CargoLineItemTO();
    cargoLineItemTO.setCargoLineItemID("Some CargoLineItem ID");
    cargoLineItemTO.setShippingMarks("All sorts of remarks!");

    LocationTO locationTO = new LocationTO();
    locationTO.setLocationName("DCSA Headquarters");
    locationTO.setAddress(address);
    locationTO.setId("1");

    ChargeTO chargeTO = new ChargeTO();
    chargeTO.setChargeType("chargeTypeCode");
    chargeTO.setCalculationBasis("CalculationBasics");
    chargeTO.setCurrencyAmount(100.0);
    chargeTO.setCurrencyCode("EUR");
    chargeTO.setQuantity(1.0);
    chargeTO.setUnitPrice(100.0);
    chargeTO.setPaymentTermCode(PaymentTerm.COL);

    CarrierClauseTO carrierClauseTO = new CarrierClauseTO();
    carrierClauseTO.setClauseContent("CarrierClause");

    CargoItemTO cargoItemTO = new CargoItemTO();
    cargoItemTO.setCargoLineItems(List.of(cargoLineItemTO));
    cargoItemTO.setWeight(10F);
    cargoItemTO.setWeightUnit(WeightUnit.KGM);
    cargoItemTO.setNumberOfPackages(1);
    cargoItemTO.setPackageCode("123");
    cargoItemTO.setEquipmentReference("EquipmentRef");

    ConsignmentItemTO consignmentItemTO =
      ConsignmentItemTO.builder()
        .descriptionOfGoods("Some description of the goods!")
        .hsCode("x".repeat(10))
        .volume(2.22)
        .weight(2.22)
        .cargoItems(List.of(cargoItemTO))
        .references(List.of(referenceTO))
        .weightUnit(WeightUnit.KGM)
        .build();

    EquipmentTO equipmentTO = new EquipmentTO();
    equipmentTO.setEquipmentReference("ref");

    UtilizedTransportEquipmentTO utilizedTransportEquipmentTO = new UtilizedTransportEquipmentTO();
    utilizedTransportEquipmentTO.setIsShipperOwned(false);
    utilizedTransportEquipmentTO.setCargoGrossWeightUnit(WeightUnit.KGM);
    utilizedTransportEquipmentTO.setCargoGrossWeight(10F);
    utilizedTransportEquipmentTO.setCargoItems(List.of(cargoItemTO));
    utilizedTransportEquipmentTO.setEquipment(equipmentTO);

    TransportTO transportTO = new TransportTO();
    transportTO.setTransportPlanStageSequenceNumber(1);
    transportTO.setTransportPlanStage(TransportPlanStageCode.MNC);
    transportTO.setLoadLocation(locationTO);
    transportTO.setDischargeLocation(locationTO);
    transportTO.setPlannedDepartureDate(OffsetDateTime.now());
    transportTO.setPlannedArrivalDate(OffsetDateTime.now());

    CommodityTO commodityTO = new CommodityTO();
    commodityTO.setCargoGrossWeight(10.0);
    commodityTO.setCargoGrossWeightUnit(WeightUnit.KGM);
    commodityTO.setCommodityType("Type");

    BookingTO bookingTO = new BookingTO();
    bookingTO.setDocumentStatus(ShipmentEventTypeCode.CMPL);
    bookingTO.setBookingRequestUpdatedDateTime(OffsetDateTime.now());
    bookingTO.setBookingRequestCreatedDateTime(OffsetDateTime.now());
    bookingTO.setCarrierBookingRequestReference("bookingTOCarrierBookingReference");
    bookingTO.setInvoicePayableAt(locationTO);

    ShipmentTO shipmentTO = new ShipmentTO();
    shipmentTO.setCarrierBookingReference("CarrierBookingReference");
    shipmentTO.setShipmentCreatedDateTime(OffsetDateTime.now());
    shipmentTO.setShipmentUpdatedDateTime(OffsetDateTime.now());
    shipmentTO.setTransports(List.of(transportTO));

    ShipmentTO approveShipmentTO = new ShipmentTO();
    approveShipmentTO.setCarrierBookingReference("CarrierBookingReference");
    approveShipmentTO.setShipmentCreatedDateTime(OffsetDateTime.now());
    approveShipmentTO.setShipmentUpdatedDateTime(OffsetDateTime.now());
    approveShipmentTO.setTransports(List.of(transportTO));
    approveShipmentTO.setBooking(bookingTO);

    ShippingInstructionTO shippingInstructionTO = new ShippingInstructionTO();
    shippingInstructionTO.setIsShippedOnboardType(true);
    shippingInstructionTO.setIsElectronic(true);
    shippingInstructionTO.setIsToOrder(true);
    shippingInstructionTO.setShippingInstructionReference(UUID.randomUUID().toString());
    shippingInstructionTO.setDocumentStatus(ShipmentEventTypeCode.RECE);
    shippingInstructionTO.setAreChargesDisplayedOnCopies(true);
    shippingInstructionTO.setUtilizedTransportEquipments(List.of(utilizedTransportEquipmentTO));
    shippingInstructionTO.setReferences(List.of(referenceTO));
    shippingInstructionTO.setConsignmentItems(List.of(consignmentItemTO));
    shippingInstructionTO.setShippingInstructionUpdatedDateTime(OffsetDateTime.now());
    shippingInstructionTO.setShippingInstructionCreatedDateTime(OffsetDateTime.now());

    ShippingInstructionTO approveShippingInstructionTO = new ShippingInstructionTO();
    approveShippingInstructionTO.setIsShippedOnboardType(true);
    approveShippingInstructionTO.setIsElectronic(true);
    approveShippingInstructionTO.setIsToOrder(true);
    approveShippingInstructionTO.setShippingInstructionReference(UUID.randomUUID().toString());
    approveShippingInstructionTO.setDocumentStatus(ShipmentEventTypeCode.PENA);
    approveShippingInstructionTO.setAreChargesDisplayedOnCopies(true);
    approveShippingInstructionTO.setUtilizedTransportEquipments(List.of(utilizedTransportEquipmentTO));
    approveShippingInstructionTO.setReferences(List.of(referenceTO));
    approveShippingInstructionTO.setConsignmentItems(List.of(consignmentItemTO));
    approveShippingInstructionTO.setShippingInstructionUpdatedDateTime(OffsetDateTime.now());
    approveShippingInstructionTO.setShippingInstructionCreatedDateTime(OffsetDateTime.now());

    PartyContactDetailsTO partyContactDetailsTO = new PartyContactDetailsTO();
    partyContactDetailsTO.setName("Maersk Incorporated");

    PartyTO partyTO = new PartyTO();
    partyTO.setId(UUID.randomUUID().toString());
    partyTO.setPartyName("Maersk");
    partyTO.setPartyContactDetails(List.of(partyContactDetailsTO));

    transportDocumentTO = new TransportDocumentTO();
    transportDocumentTO.setTransportDocumentReference("TRDocReference1");
    transportDocumentTO.setCharges(List.of(chargeTO));
    transportDocumentTO.setPlaceOfIssue(locationTO);
    transportDocumentTO.setCarrierCode("MSK");
    transportDocumentTO.setCarrierCodeListProvider(CarrierCodeListProvider.SMDG);
    transportDocumentTO.setIssuingParty(partyTO);
    transportDocumentTO.setCarrierClauses(List.of(carrierClauseTO));
    transportDocumentTO.setShippingInstruction(shippingInstructionTO);
    transportDocumentTO.setTransportDocumentCreatedDateTime(OffsetDateTime.now());

    approvedTransportDocument = new TransportDocumentRefStatusTO();
    approvedTransportDocument.setTransportDocumentReference("approvedTRDocRefer");
    approvedTransportDocument.setDocumentStatus(ShipmentEventTypeCode.APPR);
    approvedTransportDocument.setTransportDocumentCreatedDateTime(OffsetDateTime.now());
    approvedTransportDocument.setTransportDocumentUpdatedDateTime(OffsetDateTime.now());

    // request body for valid & invalid approval request body
    validTransportDocumentRequestTO = ApproveTransportDocumentRequestTO.builder()
      .documentStatus(ShipmentEventTypeCode.APPR)
      .build();
    invalidTransportDocumentRequestTO = ApproveTransportDocumentRequestTO.builder()
      .documentStatus(ShipmentEventTypeCode.RECE)
      .build();
  }

  @Test
  @DisplayName("Get transport document with valid reference should return transport document.")
  void testGetTransportDocumentByReference() {
    when(transportDocumentService.findByTransportDocumentReference(eq("TRDocReference1"))).thenReturn(Mono.just(transportDocumentTO));

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
        .hasJsonPath()
        .consumeWith(
            response ->
                JsonSchemaValidator.validateAgainstJsonSchema(response, "transportDocument.json"));
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
        .expectBody()
        .consumeWith(
            response -> JsonSchemaValidator.validateAgainstJsonSchema(response, "error.json"));
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
        .expectBody()
        .consumeWith(
            response -> JsonSchemaValidator.validateAgainstJsonSchema(response, "error.json"));
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
        .expectBody()
        .consumeWith(
            response ->
                JsonSchemaValidator.validateAgainstJsonSchema(response, "transportDocument.json"));
  }

  @Test
  @DisplayName("Approve at transport document with valid reference should return transport document with SI & booking " +
    "document statuses set to APPR & CMPL respectively")
  void testApproveTransportDocumentByReference() {

    when(transportDocumentService.approveTransportDocument("approvedTRDocReference"))
      .thenReturn(Mono.just(approvedTransportDocument));

    webTestClient
      .patch()
      .uri(
        uriBuilder ->
          uriBuilder.path(TRANSPORT_DOCUMENT_ENDPOINT).pathSegment("approvedTRDocReference").build())
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(validTransportDocumentRequestTO))
      .exchange()
      .expectStatus()
      .isOk()
      .expectBody()
      .jsonPath("$.transportDocumentReference").hasJsonPath()
      .jsonPath("$.documentStatus").hasJsonPath()
      .consumeWith(
        response ->
          JsonSchemaValidator.validateAgainstJsonSchema(response, "transportDocumentRefStatus.json"));

  }


  @Test
  @DisplayName("Approve transport document with invalid request body should return bad request")
  void testApproveTransportDocumentWithInvalidBodyRequest() {

      webTestClient
        .patch()
        .uri(
          uriBuilder ->
            uriBuilder.path(TRANSPORT_DOCUMENT_ENDPOINT).pathSegment("TRDocReference1").build())
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(invalidTransportDocumentRequestTO))
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody()
        .consumeWith(
          response -> JsonSchemaValidator.validateAgainstJsonSchema(response, "error.json"));
  }

  @Test
  @DisplayName("Approving a transport document that has a SI with invalid status document status should return bad request")
  void testApproveTransportDocumentThatHasShippingInstructionInvalidDocumentStatus() {

    when(transportDocumentService.approveTransportDocument("TRDocReference1"))
      .thenReturn(
        Mono.error(
          ConcreteRequestErrorMessageException.notFound(
            "Cannot Approve Transport Document with Shipping Instruction that is not in status DRFT")));

    webTestClient
        .patch()
        .uri(
          uriBuilder ->
            uriBuilder.path(TRANSPORT_DOCUMENT_ENDPOINT).pathSegment("TRDocReference1").build())
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(validTransportDocumentRequestTO))
        .exchange()
        .expectStatus()
        .isNotFound()
      .expectBody()
      .consumeWith(
        response -> JsonSchemaValidator.validateAgainstJsonSchema(response, "error.json"));
  }


  @Test
  @DisplayName("Approving a transport document that has a SI with no shipments should return bad request")
  void testApproveTransportDocumentThatHasShippingInstructionWithNoShipments() {

    when(transportDocumentService.approveTransportDocument("TRDocReference1"))
      .thenReturn(
        Mono.error(
          ConcreteRequestErrorMessageException.notFound(
            "No shipments found for Shipping instruction of transport document reference: " + "{transport document ID}")));

    webTestClient
      .patch()
      .uri(
        uriBuilder ->
          uriBuilder.path(TRANSPORT_DOCUMENT_ENDPOINT).pathSegment("TRDocReference1").build())
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(validTransportDocumentRequestTO))
      .exchange()
      .expectStatus()
      .isNotFound()
      .expectBody()
      .consumeWith(
        response -> JsonSchemaValidator.validateAgainstJsonSchema(response, "error.json"));
  }
}
