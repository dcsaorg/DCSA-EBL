package org.dcsa.ebl.controller;

import org.dcsa.core.events.edocumentation.model.transferobject.ConsignmentItemTO;
import org.dcsa.core.events.model.enums.*;
import org.dcsa.core.events.model.transferobjects.*;
import org.dcsa.core.exception.handler.GlobalExceptionHandler;
import org.dcsa.core.security.SecurityConfig;
import org.dcsa.ebl.model.mappers.ShippingInstructionMapper;
import org.dcsa.ebl.model.transferobjects.ShippingInstructionResponseTO;
import org.dcsa.ebl.service.ShippingInstructionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Spy;
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
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Tests for ShippingInstructionController")
@ActiveProfiles("test")
@WebFluxTest(controllers = {ShippingInstructionController.class})
@Import(value = {GlobalExceptionHandler.class, SecurityConfig.class})
class ShippingInstructionControllerTest {

  @Autowired WebTestClient webTestClient;

  @MockBean ShippingInstructionService shippingInstructionService;

  @Spy
  ShippingInstructionMapper shippingInstructionMapper =
      Mappers.getMapper(ShippingInstructionMapper.class);

  private final String SHIPPING_INSTRUCTION_ENDPOINT = "/shipping-instructions";

  private ShippingInstructionTO shippingInstructionTO;
  private ShippingInstructionResponseTO shippingInstructionResponseTO;
  ConsignmentItemTO consignmentItemTO;
  ConsignmentItemTO noCargoItemsConsignmentItemTO;

  @BeforeEach
  void init() {
    // populate DTO with relevant objects to verify json schema returned
    LocationTO locationTO = new LocationTO();
    locationTO.setId("c703277f-84ca-4816-9ccf-fad8e202d3b6");
    locationTO.setLocationName("Hamburg");
    locationTO.setAddressID(UUID.fromString("8fecc6d0-2a78-401d-948a-b9753f6b53d5"));
    locationTO.setFacilityID(UUID.fromString("74dcf8e6-4ed4-439e-a935-ec183df73013"));

    EquipmentTO equipmentTO = new EquipmentTO();
    equipmentTO.setIsoEquipmentCode("22G2");
    equipmentTO.setEquipmentReference("APZU4812090");
    equipmentTO.setTareWeight(12.12f);
    equipmentTO.setWeightUnit(WeightUnit.KGM);

    SealTO sealsTO = new SealTO();
    sealsTO.setSealNumber("1".repeat(15));
    sealsTO.setSealType(SealTypeCode.BLT);
    sealsTO.setSealSource(SealSourceCode.SHI);

    CargoLineItemTO cargoLineItemTO = new CargoLineItemTO();
    cargoLineItemTO.setCargoLineItemID("Some CargoLineItem ID");
    cargoLineItemTO.setShippingMarks("All sorts of remarks!");

    CargoItemTO cargoItemTO = new CargoItemTO();
    cargoItemTO.setCargoLineItems(List.of(cargoLineItemTO));
    cargoItemTO.setNumberOfPackages(2);
    cargoItemTO.setPackageCode("XYZ");

    ActiveReeferSettingsTO activeReeferSettingsTO = new ActiveReeferSettingsTO();
    activeReeferSettingsTO.setTemperatureUnit(TemperatureUnit.CEL);
    activeReeferSettingsTO.setHumidityMax(65f);
    activeReeferSettingsTO.setHumidityMin(20f);
    activeReeferSettingsTO.setTemperatureMax(70f);
    activeReeferSettingsTO.setTemperatureMin(-10f);
    activeReeferSettingsTO.setVentilationMax(15f);
    activeReeferSettingsTO.setVentilationMin(5f);

    UtilizedTransportEquipmentTO utilizedTransportEquipmentTO = new UtilizedTransportEquipmentTO();
    utilizedTransportEquipmentTO.setCarrierBookingReference("XYZ12345");
    utilizedTransportEquipmentTO.setEquipment(equipmentTO);
    utilizedTransportEquipmentTO.setSeals(List.of(sealsTO));
    utilizedTransportEquipmentTO.setCargoGrossWeight(120f);
    utilizedTransportEquipmentTO.setCargoGrossWeightUnit(WeightUnit.KGM);
    utilizedTransportEquipmentTO.setActiveReeferSettings(activeReeferSettingsTO);
    utilizedTransportEquipmentTO.setIsShipperOwned(true);

    PartyContactDetailsTO partyContactDetailsTO = new PartyContactDetailsTO();

    PartyTO partyTO = new PartyTO();
    partyTO.setPartyContactDetails(List.of(partyContactDetailsTO));

    DocumentPartyTO documentPartyTO = new DocumentPartyTO();
    documentPartyTO.setPartyFunction(PartyFunction.DDR);
    documentPartyTO.setIsToBeNotified(true);
    documentPartyTO.setParty(partyTO);

    ReferenceTO referenceTO = new ReferenceTO();
    referenceTO.setReferenceType(ReferenceTypeCode.EQ);
    referenceTO.setReferenceValue("Some reference value");

    consignmentItemTO = ConsignmentItemTO.builder()
      .cargoItems(List.of(cargoItemTO))
      .references(List.of(referenceTO))
      .descriptionOfGoods("Foo")
      .weight(127.12)
      .weightUnit(WeightUnit.KGM)
      .build();

    noCargoItemsConsignmentItemTO =  ConsignmentItemTO.builder()
      .cargoItems(Collections.emptyList())
      .references(List.of(referenceTO))
      .descriptionOfGoods("Foo")
      .weight(127.12)
      .weightUnit(WeightUnit.KGM)
      .build();

    shippingInstructionTO = new ShippingInstructionTO();
    shippingInstructionTO.setCarrierBookingReference("XYZ12345");
    shippingInstructionTO.setPlaceOfIssue(locationTO);
    shippingInstructionTO.setUtilizedTransportEquipments(List.of(utilizedTransportEquipmentTO));
    shippingInstructionTO.setDocumentParties(List.of(documentPartyTO));
    shippingInstructionTO.setReferences(List.of(referenceTO));
    shippingInstructionTO.setIsShippedOnboardType(true);
    shippingInstructionTO.setIsElectronic(true);
    shippingInstructionTO.setIsToOrder(true);
    shippingInstructionTO.setShippingInstructionReference(UUID.randomUUID().toString());
    shippingInstructionTO.setPlaceOfIssueID(locationTO.getId());
    shippingInstructionTO.setAreChargesDisplayedOnCopies(true);
    shippingInstructionTO.setAreChargesDisplayedOnOriginals(true);
    shippingInstructionTO.setConsignmentItems(List.of(consignmentItemTO));

    OffsetDateTime now = OffsetDateTime.now();
    shippingInstructionResponseTO =
        shippingInstructionMapper
            .dtoToShippingInstructionResponseTO(shippingInstructionTO)
            .toBuilder()
            .documentStatus(ShipmentEventTypeCode.RECE)
            .shippingInstructionCreatedDateTime(now)
            .shippingInstructionUpdatedDateTime(now)
            .build();
  }

  @Test
  @DisplayName(
      "POST shipping-instructions should return 201 and valid shipping instruction json schema.")
  void postShippingInstructionsShouldReturn201ForValidShippingInstructionRequest() {

    ArgumentCaptor<ShippingInstructionTO> argument =
        ArgumentCaptor.forClass(ShippingInstructionTO.class);

    // mock service method call
    when(shippingInstructionService.createShippingInstruction(any()))
        .thenReturn(Mono.just(shippingInstructionResponseTO));

    WebTestClient.ResponseSpec exchange =
        webTestClient
            .post()
            .uri(SHIPPING_INSTRUCTION_ENDPOINT)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(shippingInstructionTO))
            .exchange();

    checkStatus201.andThen(checkShippingInstructionResponseTOJsonSchema).apply(exchange);

    // these values are only allowed in response and not to be set via request body
    verify(shippingInstructionService).createShippingInstruction(argument.capture());
    assertNull(argument.getValue().getDocumentStatus());

  }

  @Test
  @DisplayName("POST shipping-instructions should return 400 when no cargo items are present.")
  void postShippingInstructionsShouldReturn400ForMissingCargoItems() {

    shippingInstructionTO.setConsignmentItems(List.of(noCargoItemsConsignmentItemTO));

    WebTestClient.ResponseSpec exchange =
        webTestClient
            .post()
            .uri(SHIPPING_INSTRUCTION_ENDPOINT)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(shippingInstructionTO))
            .exchange();

    checkStatus400.apply(exchange);
  }

  @Test
  @DisplayName(
      "POST shipping-instructions should return 400 when no UtilizedTransportEquipment are present.")
  void postShippingInstructionsShouldReturn400ForUtilizedTransportEquipments() {

    shippingInstructionTO.setUtilizedTransportEquipments(Collections.emptyList());

    WebTestClient.ResponseSpec exchange =
        webTestClient
            .post()
            .uri(SHIPPING_INSTRUCTION_ENDPOINT)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(shippingInstructionTO))
            .exchange();

    checkStatus400.apply(exchange);
  }

  @Test
  @DisplayName("POST booking should return 400 for invalid request.")
  void postShippingInstructionsShouldReturn400ForInvalidShippingInstructionRequest() {

    ShippingInstructionTO invalidShippingInstructionTO = new ShippingInstructionTO();

    WebTestClient.ResponseSpec exchange =
        webTestClient
            .post()
            .uri(SHIPPING_INSTRUCTION_ENDPOINT)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(invalidShippingInstructionTO))
            .exchange();

    checkStatus400.apply(exchange);
  }

  @Test
  @DisplayName(
      "PUT shipping-instructions should return 200 and valid shipping instruction json schema.")
  void putShippingInstructionsShouldReturn201ForValidShippingInstructionRequest() {

    ArgumentCaptor<ShippingInstructionTO> argument =
        ArgumentCaptor.forClass(ShippingInstructionTO.class);

    // mock service method call
    when(shippingInstructionService.updateShippingInstructionByShippingInstructionReference(
            any(), any()))
        .thenReturn(Mono.just(shippingInstructionResponseTO));

    WebTestClient.ResponseSpec exchange =
        webTestClient
            .put()
            .uri(SHIPPING_INSTRUCTION_ENDPOINT + "/" + UUID.randomUUID())
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(shippingInstructionTO))
            .exchange();

    // these values are only allowed in response and not to be set via request body
    verify(shippingInstructionMapper).dtoToShippingInstructionResponseTO(argument.capture());
    assertNull(argument.getValue().getDocumentStatus());

    checkStatus200.andThen(checkShippingInstructionResponseTOJsonSchema).apply(exchange);
  }

  @Test
  @DisplayName("PUT booking should return 400 for invalid request.")
  void putShippingInstructionsShouldReturn400ForInvalidShippingInstructionRequest() {

    ShippingInstructionTO invalidShippingInstructionTO = new ShippingInstructionTO();

    WebTestClient.ResponseSpec exchange =
        webTestClient
            .put()
            .uri(SHIPPING_INSTRUCTION_ENDPOINT + "/" + UUID.randomUUID())
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(invalidShippingInstructionTO))
            .exchange();

    checkStatus400.apply(exchange);
  }

  @Test
  @DisplayName("GET booking should return 404 for invalid shipping instruction reference.")
  void getShippingInstructionsShouldReturn404ForInvalidShippingInstructionReference() {

    when(shippingInstructionService.findByReference(any())).thenReturn(Mono.empty());

    WebTestClient.ResponseSpec exchange =
        webTestClient.get().uri(SHIPPING_INSTRUCTION_ENDPOINT + "/" + UUID.randomUUID()).exchange();

    checkStatus500.apply(exchange);
  }

  private final Function<WebTestClient.ResponseSpec, WebTestClient.ResponseSpec> checkStatus200 =
      (exchange) -> exchange.expectStatus().isOk();

  private final Function<WebTestClient.ResponseSpec, WebTestClient.ResponseSpec> checkStatus201 =
      (exchange) -> exchange.expectStatus().isCreated();

  private final Function<WebTestClient.ResponseSpec, WebTestClient.ResponseSpec> checkStatus400 =
      (exchange) -> exchange.expectStatus().isBadRequest();

  private final Function<WebTestClient.ResponseSpec, WebTestClient.ResponseSpec> checkStatus500 =
      (exchange) -> exchange.expectStatus().is5xxServerError();

  private final Function<WebTestClient.ResponseSpec, WebTestClient.BodyContentSpec>
      checkShippingInstructionResponseTOJsonSchema =
          (exchange) ->
              exchange
                  .expectBody()
                  .consumeWith(System.out::println)
                  .jsonPath("$.shippingInstructionReference")
                  .hasJsonPath()
                  .jsonPath("$.documentStatus")
                  .hasJsonPath()
                  .jsonPath("$.shippingInstructionCreatedDateTime")
                  .hasJsonPath()
                  .jsonPath("$.shippingInstructionUpdatedDateTime")
                  .hasJsonPath();
}
