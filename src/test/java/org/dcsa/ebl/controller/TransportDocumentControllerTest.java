package org.dcsa.ebl.controller;

import org.dcsa.core.events.edocumentation.model.transferobject.*;
import org.dcsa.core.events.model.Address;
import org.dcsa.core.events.model.enums.*;
import org.dcsa.core.events.model.transferobjects.*;
import org.dcsa.core.exception.handler.GlobalExceptionHandler;
import org.dcsa.core.security.SecurityConfig;
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

  @BeforeEach
  private void init() {

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

    CargoItemTO cargoItemTO = new CargoItemTO();
    cargoItemTO.setHsCode("hs");
    cargoItemTO.setWeight(10F);
    cargoItemTO.setWeightUnit(WeightUnit.KGM);
    cargoItemTO.setDescriptionOfGoods("desc");
    cargoItemTO.setNumberOfPackages(1);
    cargoItemTO.setPackageCode("123");

    EquipmentTO equipmentTO = new EquipmentTO();
    equipmentTO.setEquipmentReference("ref");

    ShipmentEquipmentTO shipmentEquipmentTO = new ShipmentEquipmentTO();
    shipmentEquipmentTO.setIsShipperOwned(false);
    shipmentEquipmentTO.setCargoGrossWeightUnit(WeightUnit.KGM);
    shipmentEquipmentTO.setCargoGrossWeight(10F);
    shipmentEquipmentTO.setCargoItems(List.of(cargoItemTO));
    shipmentEquipmentTO.setEquipment(equipmentTO);

    ShippingInstructionTO shippingInstructionTO = new ShippingInstructionTO();
    shippingInstructionTO.setIsShippedOnboardType(true);
    shippingInstructionTO.setIsElectronic(true);
    shippingInstructionTO.setIsToOrder(true);
    shippingInstructionTO.setShippingInstructionID(UUID.randomUUID().toString());
    shippingInstructionTO.setDocumentStatus(ShipmentEventTypeCode.RECE);
    shippingInstructionTO.setPlaceOfIssueID(locationTO.getId());
    shippingInstructionTO.setAreChargesDisplayedOnCopies(true);
    shippingInstructionTO.setShipmentEquipments(List.of(shipmentEquipmentTO));

    TransportTO transportTO = new TransportTO();
    transportTO.setTransportPlanStageSequenceNumber(1);
    transportTO.setTransportPlanStage(TransportPlanStageCode.MNC);
    transportTO.setLoadLocation(locationTO);
    transportTO.setDischargeLocation(locationTO);
    transportTO.setPlannedDepartureDate(OffsetDateTime.now());
    transportTO.setPlannedArrivalDate(OffsetDateTime.now());

    CommodityTO commodityTO = new CommodityTO();
    commodityTO.setCargoGrossWeight(10.0);
    commodityTO.setCargoGrossWeightUnit(CargoGrossWeight.KGM);
    commodityTO.setCommodityType("Type");

    ShipmentTO shipmentTO = new ShipmentTO();
    shipmentTO.setCarrierBookingReference("CarrierBookingReference");
    shipmentTO.setShipmentCreatedDateTime(OffsetDateTime.now());
    shipmentTO.setShipmentUpdatedDateTime(OffsetDateTime.now());

    shipmentTO.setTransports(List.of(transportTO));
    shippingInstructionTO.setShipments(List.of(shipmentTO));

    transportDocumentTO = new TransportDocumentTO();
    transportDocumentTO.setTransportDocumentReference("TransportDocumentReference1");
    transportDocumentTO.setCharges(List.of(chargeTO));
    transportDocumentTO.setPlaceOfIssue(locationTO);
    transportDocumentTO.setCarrierClauses(List.of(carrierClauseTO));
    transportDocumentTO.setShippingInstruction(shippingInstructionTO);
    transportDocumentTO.setTransportDocumentReference("TRDocReference1");
    transportDocumentTO.setTransportDocumentCreatedDateTime(OffsetDateTime.now());
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
}
