package org.dcsa.ebl.service.impl;

import org.dcsa.core.events.edocumentation.model.transferobject.*;
import org.dcsa.core.events.edocumentation.service.*;
import org.dcsa.core.events.model.*;
import org.dcsa.core.events.model.enums.*;
import org.dcsa.core.events.model.transferobjects.CargoItemTO;
import org.dcsa.core.events.model.transferobjects.CargoLineItemTO;
import org.dcsa.core.events.model.transferobjects.ReferenceTO;
import org.dcsa.core.events.model.transferobjects.ShippingInstructionTO;
import org.dcsa.core.events.repository.BookingRepository;
import org.dcsa.core.events.repository.ShipmentRepository;
import org.dcsa.core.events.repository.TransportDocumentRepository;
import org.dcsa.core.events.service.ShipmentEventService;
import org.dcsa.core.exception.ConcreteRequestErrorMessageException;
import org.dcsa.ebl.model.mappers.TransportDocumentMapper;
import org.dcsa.ebl.model.transferobjects.TransportDocumentTO;
import org.dcsa.ebl.repository.ShippingInstructionRepository;
import org.dcsa.ebl.service.ShippingInstructionService;
import org.dcsa.skernel.model.Address;
import org.dcsa.skernel.model.Carrier;
import org.dcsa.skernel.model.enums.CarrierCodeListProvider;
import org.dcsa.skernel.model.enums.FacilityCodeListProvider;
import org.dcsa.skernel.model.transferobjects.LocationTO;
import org.dcsa.skernel.repositority.CarrierRepository;
import org.dcsa.skernel.service.LocationService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.dcsa.core.events.model.enums.ShipmentEventTypeCode.BOOKING_DOCUMENT_STATUSES;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests for TransportDocument Implementation.")
class TransportDocumentServiceImplTest {

  @Mock TransportDocumentRepository transportDocumentRepository;
  @Mock CarrierRepository carrierRepository;
  @Mock ShippingInstructionRepository shippingInstructionRepository;
  @Mock BookingRepository bookingRepository;
  @Mock ShipmentRepository shipmentRepository;

  @Mock ShippingInstructionService shippingInstructionService;
  @Mock ChargeService chargeService;
  @Mock CarrierClauseService carrierClauseService;
  @Mock LocationService locationService;
  @Mock ShipmentService shipmentService;
  @Mock ShipmentEventService shipmentEventService;
  @Mock TransportService transportService;
  @Mock ShipmentLocationService shipmentLocationService;

  @InjectMocks TransportDocumentServiceImpl transportDocumentServiceImpl;

  @Spy
  TransportDocumentMapper transportDocumentMapper =
      Mappers.getMapper(TransportDocumentMapper.class);

  CargoItem cargoItem;
  Shipment shipment;
  UtilizedTransportEquipment utilizedTransportEquipment;
  TransportDocument transportDocument;
  ShippingInstruction shippingInstruction;
  Carrier carrier;
  Booking booking;

  TransportDocumentTO transportDocumentTO;
  ShippingInstructionTO shippingInstructionTO;
  ChargeTO chargeTO;
  CarrierClauseTO carrierClauseTO;
  LocationTO locationTO;
  ShipmentTO shipmentTO;
  BookingTO bookingTO;
  Reference reference;
  CargoItemTO cargoItemTO;
  ConsignmentItemTO consignmentItemTO;
  CargoLineItemTO cargoLineItemTO;
  Address address;
  ReferenceTO referenceTO;
  TransportTO transportTO;

  @BeforeEach
  void init() {
    initEntities();
    initTO();
  }

  private void initEntities() {
    OffsetDateTime now = OffsetDateTime.now();
    carrier = new Carrier();
    carrier.setId(UUID.randomUUID());
    carrier.setNmftaCode("x".repeat(4));
    carrier.setSmdgCode(null);
    carrier.setCarrierName("Klods-Hans");

    shippingInstruction = new ShippingInstruction();
    shippingInstruction.setId(UUID.randomUUID());
    shippingInstruction.setShippingInstructionReference(UUID.randomUUID().toString());
    shippingInstruction.setDocumentStatus(ShipmentEventTypeCode.RECE);
    shippingInstruction.setShippingInstructionCreatedDateTime(now);
    shippingInstruction.setShippingInstructionUpdatedDateTime(now);

    shipment = new Shipment();
    shipment.setShipmentID(UUID.randomUUID());
    shipment.setCarrierBookingReference("x".repeat(35));
    shipment.setTermsAndConditions("This is a terms and condition!");

    utilizedTransportEquipment = new UtilizedTransportEquipment();
    utilizedTransportEquipment.setId(UUID.randomUUID());
    utilizedTransportEquipment.setIsShipperOwned(false);
    utilizedTransportEquipment.setCargoGrossWeightUnit(WeightUnit.KGM);
    utilizedTransportEquipment.setCargoGrossWeight(21f);

    cargoItem = new CargoItem();
    cargoItem.setId(UUID.randomUUID());
    cargoItem.setNumberOfPackages(2);
    cargoItem.setPackageCode("XYZ");
    cargoItem.setUtilizedTransportEquipmentID(utilizedTransportEquipment.getId());
    cargoItem.setShippingInstructionID(
        shippingInstruction.getId());

    reference = new Reference();
    reference.setReferenceValue("test");
    reference.setReferenceType(ReferenceTypeCode.FF);
    cargoItem.setShippingInstructionID(shippingInstruction.getId());

    transportDocument = new TransportDocument();
    transportDocument.setShippingInstructionID(shippingInstruction.getId());
    transportDocument.setTransportDocumentReference("TransportDocumentReference1");
    transportDocument.setIssuer(carrier.getId());
    transportDocument.setIssueDate(LocalDate.now());
    transportDocument.setTransportDocumentCreatedDateTime(now);
    transportDocument.setTransportDocumentUpdatedDateTime(now);
    transportDocument.setDeclaredValue(12f);
    transportDocument.setDeclaredValueCurrency("DKK");
    transportDocument.setReceivedForShipmentDate(LocalDate.now());
    transportDocument.setPlaceOfIssue("1");

    address = new Address();
    address.setCity("Amsterdam");
    address.setCountry("Netherlands");
    address.setStreet("Strawinskylaan");
    address.setPostalCode("1077ZX");
    address.setStreetNumber("4117");
    address.setFloor("6");
    address.setStateRegion("Noord-Holland");

    booking = new Booking();
    booking.setId(UUID.randomUUID());
    booking.setDocumentStatus(ShipmentEventTypeCode.CONF);
    booking.setCarrierBookingRequestReference(UUID.randomUUID().toString());
    booking.setReceiptTypeAtOrigin(ReceiptDeliveryType.CY);
    booking.setDeliveryTypeAtDestination(ReceiptDeliveryType.SD);
    booking.setCargoMovementTypeAtDestination(CargoMovementType.FCL);
    booking.setCargoMovementTypeAtOrigin(CargoMovementType.LCL);
    booking.setServiceContractReference("x".repeat(30));
  }

  private void initTO() {

    referenceTO = new ReferenceTO();
    referenceTO.setReferenceType(reference.getReferenceType());
    referenceTO.setReferenceValue(reference.getReferenceValue());

    cargoLineItemTO = new CargoLineItemTO();
    cargoLineItemTO.setCargoLineItemID("Some CargoLineItem ID");
    cargoLineItemTO.setShippingMarks("All sorts of remarks!");

    cargoItemTO = new CargoItemTO();
    cargoItemTO.setCargoLineItems(List.of(cargoLineItemTO));
    cargoItemTO.setNumberOfPackages(2);
    cargoItemTO.setPackageCode("XYZ");

    locationTO = new LocationTO();
    locationTO.setLocationName("DCSA Headquarters");
    locationTO.setAddress(address);
    locationTO.setId("1");

    chargeTO = new ChargeTO();
    chargeTO.setChargeType("chargeTypeCode");
    chargeTO.setCalculationBasis("CalculationBasics");
    chargeTO.setCurrencyAmount(100.0);
    chargeTO.setCurrencyCode("EUR");
    chargeTO.setQuantity(1.0);
    chargeTO.setUnitPrice(100.0);
    chargeTO.setPaymentTermCode(PaymentTerm.COL);

    carrierClauseTO = new CarrierClauseTO();
    carrierClauseTO.setClauseContent("CarrierClause");

    consignmentItemTO =
        ConsignmentItemTO.builder()
            .descriptionOfGoods("Some description of the goods!")
            .hsCode("x".repeat(10))
            .volume(2.22)
            .weight(2.22)
            .cargoItems(List.of(cargoItemTO))
            .references(List.of(referenceTO))
            .build();

    bookingTO = new BookingTO();
    bookingTO.setDocumentStatus(ShipmentEventTypeCode.PENA);
    bookingTO.setCarrierBookingRequestReference("TransportDocumentReference1");

    shipmentTO = new ShipmentTO();
    shipmentTO.setCarrierBookingReference("TransportDocumentReference1");
    shipmentTO.setBooking(bookingTO);

    shippingInstructionTO = new ShippingInstructionTO();
    shippingInstructionTO.setIsShippedOnboardType(true);
    shippingInstructionTO.setIsElectronic(true);
    shippingInstructionTO.setIsToOrder(true);
    shippingInstructionTO.setShippingInstructionReference(UUID.randomUUID().toString());
    shippingInstructionTO.setDocumentStatus(ShipmentEventTypeCode.DRFT);
    shippingInstructionTO.setPlaceOfIssueID(locationTO.getId());
    shippingInstructionTO.setAreChargesDisplayedOnCopies(true);
    shippingInstructionTO.setShippingInstructionUpdatedDateTime(OffsetDateTime.now());
    shippingInstructionTO.setReferences(List.of(referenceTO));
    shippingInstructionTO.setConsignmentItems(List.of(consignmentItemTO));

    transportDocumentTO = new TransportDocumentTO();
    transportDocumentTO.setCharges(List.of(chargeTO));
    transportDocumentTO.setPlaceOfIssue(locationTO);
    transportDocumentTO.setCarrierClauses(List.of(carrierClauseTO));
    transportDocumentTO.setShippingInstruction(shippingInstructionTO);
    transportDocumentTO.setTransportDocumentReference("TransportDocumentReference1");

    LocationTO dischargeLocation = new LocationTO();
    dischargeLocation.setFacilityCode("123456");
    dischargeLocation.setFacilityCodeListProvider(FacilityCodeListProvider.SMDG);
    dischargeLocation.setId("7bf6f428-58f0-4347-9ce8-d6be2f5d5745");
    dischargeLocation.setAddressID(UUID.fromString("8fecc6d0-2a78-401d-948a-b9753f6b53d5"));
    dischargeLocation.setFacilityID(UUID.fromString("74dcf8e6-4ed4-439e-a935-ec183df73013"));

    LocationTO loadLocation = new LocationTO();
    loadLocation.setFacilityCode("654321");
    loadLocation.setFacilityCodeListProvider(FacilityCodeListProvider.SMDG);
    loadLocation.setId("c703277f-84ca-4816-9ccf-fad8e202d3b6");

    transportTO = new TransportTO();
    transportTO.setTransportPlanStageSequenceNumber(1);
    transportTO.setTransportPlanStage(TransportPlanStageCode.ONC);
    transportTO.setIsUnderShippersResponsibility(false);
    transportTO.setModeOfTransport(DCSATransportType.VESSEL);
    transportTO.setVesselName("vesselName");
    transportTO.setVesselIMONumber("9876543");
    transportTO.setImportVoyageNumber("1234E");
    transportTO.setExportVoyageNumber("1234W");
    transportTO.setPlannedArrivalDate(OffsetDateTime.now());
    transportTO.setPlannedDepartureDate(OffsetDateTime.now());
    transportTO.setTransportName("TransportName");
    transportTO.setTransportReference("TrRef1");
    transportTO.setDischargeLocation(dischargeLocation);
    transportTO.setLoadLocation(loadLocation);
  }

  @Nested
  @DisplayName("Tests for the method findAll(#DocumentStatus,#CarrierBookingReference)")
  class GetTransportDocumentSummaryTest {

    @Test
    @DisplayName("Test GET shipping instruction with everything for a valid ID.")
    void testGetTransportDocumentWithEverythingForValidID() {
      when(carrierRepository.findById(any(UUID.class))).thenReturn(Mono.just(carrier));
      when(shippingInstructionRepository.findById(any(UUID.class)))
          .thenReturn(Mono.just(shippingInstruction));
      when(shippingInstructionRepository.findCarrierBookingReferenceByShippingInstructionID(any()))
          .thenReturn(Flux.just(shipment.getCarrierBookingReference()));
      StepVerifier.create(transportDocumentServiceImpl.mapDM2TO(transportDocument))
          .assertNext(
              result -> {
                assertNotNull(result.getTransportDocumentReference());
                assertNotNull(result.getIssuerCode());
                assertNotNull(result.getIssueDate());
                assertNotNull(result.getTransportDocumentCreatedDateTime());
                assertNotNull(result.getTransportDocumentUpdatedDateTime());
                assertNotNull(result.getShippingInstructionReference());
                assertNotNull(result.getReceivedForShipmentDate());
                assertNotNull(result.getDeclaredValue());
                assertNotNull(result.getDeclaredValueCurrency());
                assertNotNull(result.getIssuerCodeListProvider());
                assertFalse(result.getCarrierBookingReferences().isEmpty());
                assertEquals(shippingInstruction.getDocumentStatus(), result.getDocumentStatus());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName("Test GET shipping instruction without carrierBookingReferences.")
    void testGetTransportDocumentWithoutCarrierBookingReferences() {
      when(carrierRepository.findById(any(UUID.class))).thenReturn(Mono.just(carrier));
      when(shippingInstructionRepository.findById((UUID) any()))
          .thenReturn(Mono.just(shippingInstruction));
      when(shippingInstructionRepository.findCarrierBookingReferenceByShippingInstructionID(any()))
          .thenReturn(Flux.empty());

      StepVerifier.create(transportDocumentServiceImpl.mapDM2TO(transportDocument))
          .assertNext(
              result -> {
                assertNotNull(result.getTransportDocumentReference());
                assertNotNull(result.getIssuerCode());
                assertNotNull(result.getIssueDate());
                assertNotNull(result.getTransportDocumentCreatedDateTime());
                assertNotNull(result.getTransportDocumentUpdatedDateTime());
                assertNotNull(result.getShippingInstructionReference());
                assertNotNull(result.getReceivedForShipmentDate());
                assertNotNull(result.getDeclaredValue());
                assertNotNull(result.getDeclaredValueCurrency());
                assertNotNull(result.getIssuerCodeListProvider());
                assertTrue(result.getCarrierBookingReferences().isEmpty());
                assertEquals(shippingInstruction.getDocumentStatus(), result.getDocumentStatus());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName("Test GET transport document summaries for null issuer")
    void testGetTransportDocumentWithNullIssuer() {

      transportDocument.setIssuer(null);

      when(shippingInstructionRepository.findById((UUID) any()))
          .thenReturn(Mono.just(shippingInstruction));
      when(shippingInstructionRepository.findCarrierBookingReferenceByShippingInstructionID(any()))
          .thenReturn(Flux.just(shipment.getCarrierBookingReference()));

      StepVerifier.create(transportDocumentServiceImpl.mapDM2TO(transportDocument))
          .assertNext(
              result -> {
                verify(carrierRepository, never()).findById(any(UUID.class));

                assertNotNull(result.getTransportDocumentCreatedDateTime());
                assertNotNull(result.getTransportDocumentUpdatedDateTime());
                assertEquals(shippingInstruction.getDocumentStatus(), result.getDocumentStatus());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName("Test GET transport document summaries for invalid issuer")
    void testGetTransportDocumentWithInvalidIssuer() {

      when(shippingInstructionRepository.findById((UUID) any()))
          .thenReturn(Mono.just(shippingInstruction));
      when(shippingInstructionRepository.findCarrierBookingReferenceByShippingInstructionID(any()))
          .thenReturn(Flux.just(shipment.getCarrierBookingReference()));
      when(carrierRepository.findById(any(UUID.class))).thenReturn(Mono.empty());

      StepVerifier.create(transportDocumentServiceImpl.mapDM2TO(transportDocument))
          .expectErrorSatisfies(
              throwable -> {
                Assertions.assertTrue(throwable instanceof ConcreteRequestErrorMessageException);
                assertEquals(
                    "No carrier found with issuer ID: " + transportDocument.getIssuer(),
                    throwable.getMessage());
              })
          .verify();
    }

    @Test
    @DisplayName("Test GET transport document summaries for invalid shipping instruction")
    void testGetTransportDocumentWithInvalidShippingInstruction() {

      when(shippingInstructionRepository.findById(any(UUID.class))).thenReturn(Mono.empty());

      StepVerifier.create(transportDocumentServiceImpl.mapDM2TO(transportDocument))
          .expectErrorSatisfies(
              throwable -> {
                Assertions.assertTrue(throwable instanceof ConcreteRequestErrorMessageException);
                assertEquals(
                    "No shipping instruction was found with reference: "
                        + transportDocument.getShippingInstructionID(),
                    throwable.getMessage());
              })
          .verify();
    }
  }

  @Nested
  @DisplayName("Tests for the method findById(#TransportDocumentReference)")
  class GetTransportDocumentTOTest {

    @Test
    @DisplayName("Get transport document with reference should return transport document")
    void testFindTransportDocument() {

      carrier.setNmftaCode(null);

      when(shipmentRepository.findByCarrierBookingReferenceAndValidUntilIsNull(any())).thenReturn(Mono.just(shipment));
      when(bookingRepository.findCarrierBookingReferenceAndValidUntilIsNull(any())).thenReturn(Mono.just(booking));
      when(transportDocumentRepository.findLatestTransportDocumentByTransportDocumentReference(any()))
          .thenReturn(Mono.just(transportDocument));
      when(carrierRepository.findById((UUID) any())).thenReturn(Mono.just(carrier));
      when(locationService.fetchLocationDeepObjByID(shippingInstructionTO.getPlaceOfIssueID()))
          .thenReturn(Mono.just(locationTO));
      when(shippingInstructionService.findByID(transportDocument.getShippingInstructionID()))
          .thenReturn(Mono.just(shippingInstructionTO));
      when(chargeService.fetchChargesByTransportDocumentID(transportDocument.getId()))
          .thenReturn(Flux.just(chargeTO));
      when(carrierClauseService.fetchCarrierClausesByTransportDocumentID(any()))
          .thenReturn(Flux.just(carrierClauseTO));
      when(transportService.findByCarrierBookingReference(any())).thenReturn(Flux.just(transportTO));
      when(shipmentLocationService.fetchShipmentLocationByTransportDocumentID(any())).thenReturn(Mono.empty());

      StepVerifier.create(
              transportDocumentServiceImpl.findByTransportDocumentReference(
                  "TransportDocumentReference1"))
          .assertNext(
              transportDocumentTOResponse -> {
                assertEquals(carrier.getSmdgCode(), transportDocumentTOResponse.getIssuerCode());
                assertEquals(1, transportDocumentTOResponse.getCharges().size());
                assertNotNull(transportDocumentTOResponse.getPlaceOfIssue());
                assertEquals(1, transportDocumentTOResponse.getCarrierClauses().size());
                assertEquals(1, transportDocumentTOResponse.getTransports().size());
                assertNotNull(transportDocumentTOResponse.getReceiptTypeAtOrigin());
                assertNotNull(transportDocumentTOResponse.getDeliveryTypeAtDestination());
                assertNotNull(transportDocumentTOResponse.getCargoMovementTypeAtOrigin());
                assertNotNull(transportDocumentTOResponse.getCargoMovementTypeAtDestination());
                assertNotNull(transportDocumentTOResponse.getServiceContractReference());
                assertNotNull(transportDocumentTOResponse.getTermsAndConditions());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Get transport document without place of issue should return transport document without place of issue")
    void testFindTransportDocumentWithoutPlaceOfIssue() {

      carrier.setNmftaCode(null);

      transportDocument.setPlaceOfIssue(null);
      when(shipmentRepository.findByCarrierBookingReferenceAndValidUntilIsNull(any())).thenReturn(Mono.just(shipment));
      when(bookingRepository.findCarrierBookingReferenceAndValidUntilIsNull(any())).thenReturn(Mono.just(booking));
      when(transportDocumentRepository.findLatestTransportDocumentByTransportDocumentReference(any()))
          .thenReturn(Mono.just(transportDocument));
      when(carrierRepository.findById((UUID) any())).thenReturn(Mono.just(carrier));
      when(shippingInstructionService.findByID(transportDocument.getShippingInstructionID()))
          .thenReturn(Mono.just(shippingInstructionTO));
      when(chargeService.fetchChargesByTransportDocumentID(transportDocument.getId()))
          .thenReturn(Flux.just(chargeTO));
      when(carrierClauseService.fetchCarrierClausesByTransportDocumentID(any()))
          .thenReturn(Flux.just(carrierClauseTO));
      when(transportService.findByCarrierBookingReference(any())).thenReturn(Flux.just(transportTO));
      when(shipmentLocationService.fetchShipmentLocationByTransportDocumentID(any())).thenReturn(Mono.empty());

      StepVerifier.create(
              transportDocumentServiceImpl.findByTransportDocumentReference(
                  "TransportDocumentReference1"))
          .assertNext(
              transportDocumentTOResponse -> {
                assertEquals(carrier.getSmdgCode(), transportDocumentTOResponse.getIssuerCode());
                assertEquals(1, transportDocumentTOResponse.getCharges().size());
                assertNull(transportDocumentTOResponse.getPlaceOfIssue());
                assertEquals(1, transportDocumentTOResponse.getCarrierClauses().size());
                assertEquals(1, transportDocumentTOResponse.getTransports().size());
                assertNotNull(transportDocumentTOResponse.getReceiptTypeAtOrigin());
                assertNotNull(transportDocumentTOResponse.getDeliveryTypeAtDestination());
                assertNotNull(transportDocumentTOResponse.getCargoMovementTypeAtOrigin());
                assertNotNull(transportDocumentTOResponse.getCargoMovementTypeAtDestination());
                assertNotNull(transportDocumentTOResponse.getServiceContractReference());
                assertNotNull(transportDocumentTOResponse.getTermsAndConditions());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Get transport document without top level carrierBookingReferences should return transport document without carrierBookingReferences on top level.")
    void testFindTransportDocumentWithoutCarrierBookingReferences() {

      carrier.setNmftaCode(null);

      when(shipmentRepository.findByCarrierBookingReferenceAndValidUntilIsNull(any())).thenReturn(Mono.just(shipment));
      when(bookingRepository.findCarrierBookingReferenceAndValidUntilIsNull(any())).thenReturn(Mono.just(booking));
      when(transportDocumentRepository.findLatestTransportDocumentByTransportDocumentReference(any()))
          .thenReturn(Mono.just(transportDocument));
      when(carrierRepository.findById((UUID) any())).thenReturn(Mono.just(carrier));
      when(locationService.fetchLocationDeepObjByID(shippingInstructionTO.getPlaceOfIssueID()))
          .thenReturn(Mono.just(locationTO));
      when(shippingInstructionService.findByID(transportDocument.getShippingInstructionID()))
          .thenReturn(Mono.just(shippingInstructionTO));
      when(chargeService.fetchChargesByTransportDocumentID(transportDocument.getId()))
          .thenReturn(Flux.just(chargeTO));
      when(carrierClauseService.fetchCarrierClausesByTransportDocumentID(any()))
          .thenReturn(Flux.just(carrierClauseTO));
      when(transportService.findByCarrierBookingReference(any())).thenReturn(Flux.just(transportTO));
      when(shipmentLocationService.fetchShipmentLocationByTransportDocumentID(any())).thenReturn(Mono.empty());

      StepVerifier.create(
              transportDocumentServiceImpl.findByTransportDocumentReference(
                  "TransportDocumentReference1"))
          .assertNext(
              transportDocumentTOResponse -> {
                assertEquals(carrier.getSmdgCode(), transportDocumentTOResponse.getIssuerCode());
                assertEquals(1, transportDocumentTOResponse.getCharges().size());
                assertNotNull(transportDocumentTOResponse.getPlaceOfIssue());
                assertEquals(1, transportDocumentTOResponse.getCarrierClauses().size());
                assertEquals(1, transportDocumentTOResponse.getTransports().size());
                assertNotNull(transportDocumentTOResponse.getReceiptTypeAtOrigin());
                assertNotNull(transportDocumentTOResponse.getDeliveryTypeAtDestination());
                assertNotNull(transportDocumentTOResponse.getCargoMovementTypeAtOrigin());
                assertNotNull(transportDocumentTOResponse.getCargoMovementTypeAtDestination());
                assertNotNull(transportDocumentTOResponse.getServiceContractReference());
                assertNotNull(transportDocumentTOResponse.getTermsAndConditions());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Get transport document without charges should return transport document without charges")
    void testFindTransportDocumentWithoutCharges() {

      carrier.setNmftaCode(null);

      when(shipmentRepository.findByCarrierBookingReferenceAndValidUntilIsNull(any())).thenReturn(Mono.just(shipment));
      when(bookingRepository.findCarrierBookingReferenceAndValidUntilIsNull(any())).thenReturn(Mono.just(booking));
      when(transportDocumentRepository.findLatestTransportDocumentByTransportDocumentReference(any()))
          .thenReturn(Mono.just(transportDocument));
      when(carrierRepository.findById((UUID) any())).thenReturn(Mono.just(carrier));
      when(locationService.fetchLocationDeepObjByID(shippingInstructionTO.getPlaceOfIssueID()))
          .thenReturn(Mono.just(locationTO));
      when(shippingInstructionService.findByID(transportDocument.getShippingInstructionID()))
          .thenReturn(Mono.just(shippingInstructionTO));
      when(chargeService.fetchChargesByTransportDocumentID(transportDocument.getId()))
          .thenReturn(Flux.empty());
      when(carrierClauseService.fetchCarrierClausesByTransportDocumentID(any()))
          .thenReturn(Flux.just(carrierClauseTO));
      when(transportService.findByCarrierBookingReference(any())).thenReturn(Flux.just(transportTO));
      when(shipmentLocationService.fetchShipmentLocationByTransportDocumentID(any())).thenReturn(Mono.empty());

      StepVerifier.create(
              transportDocumentServiceImpl.findByTransportDocumentReference(
                  "TransportDocumentReference1"))
          .assertNext(
              transportDocumentTOResponse -> {
                assertEquals(carrier.getSmdgCode(), transportDocumentTOResponse.getIssuerCode());
                assertEquals(0, transportDocumentTOResponse.getCharges().size());
                assertNotNull(transportDocumentTOResponse.getPlaceOfIssue());
                assertEquals(1, transportDocumentTOResponse.getCarrierClauses().size());
                assertEquals(1, transportDocumentTOResponse.getTransports().size());
                assertNotNull(transportDocumentTOResponse.getReceiptTypeAtOrigin());
                assertNotNull(transportDocumentTOResponse.getDeliveryTypeAtDestination());
                assertNotNull(transportDocumentTOResponse.getCargoMovementTypeAtOrigin());
                assertNotNull(transportDocumentTOResponse.getCargoMovementTypeAtDestination());
                assertNotNull(transportDocumentTOResponse.getServiceContractReference());
                assertNotNull(transportDocumentTOResponse.getTermsAndConditions());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Get transport document without carrier clauses should return transport document without carrier clauses")
    void testFindTransportDocumentWithoutCarrierClauses() {

      carrier.setNmftaCode(null);

      when(shipmentRepository.findByCarrierBookingReferenceAndValidUntilIsNull(any())).thenReturn(Mono.just(shipment));
      when(bookingRepository.findCarrierBookingReferenceAndValidUntilIsNull(any())).thenReturn(Mono.just(booking));
      when(transportDocumentRepository.findLatestTransportDocumentByTransportDocumentReference(any()))
          .thenReturn(Mono.just(transportDocument));
      when(carrierRepository.findById((UUID) any())).thenReturn(Mono.just(carrier));
      when(locationService.fetchLocationDeepObjByID(shippingInstructionTO.getPlaceOfIssueID()))
          .thenReturn(Mono.just(locationTO));
      when(shippingInstructionService.findByID(transportDocument.getShippingInstructionID()))
          .thenReturn(Mono.just(shippingInstructionTO));
      when(chargeService.fetchChargesByTransportDocumentID(transportDocument.getId()))
          .thenReturn(Flux.just(chargeTO));
      when(carrierClauseService.fetchCarrierClausesByTransportDocumentID(any()))
          .thenReturn(Flux.empty());
      when(transportService.findByCarrierBookingReference(any())).thenReturn(Flux.just(transportTO));
      when(shipmentLocationService.fetchShipmentLocationByTransportDocumentID(any())).thenReturn(Mono.empty());

      StepVerifier.create(
              transportDocumentServiceImpl.findByTransportDocumentReference(
                  "TransportDocumentReference1"))
          .assertNext(
              transportDocumentTOResponse -> {
                assertEquals(carrier.getSmdgCode(), transportDocumentTOResponse.getIssuerCode());
                assertEquals(1, transportDocumentTOResponse.getCharges().size());
                assertNotNull(transportDocumentTOResponse.getPlaceOfIssue());
                assertEquals(0, transportDocumentTOResponse.getCarrierClauses().size());
                assertEquals(1, transportDocumentTOResponse.getTransports().size());
                assertNotNull(transportDocumentTOResponse.getReceiptTypeAtOrigin());
                assertNotNull(transportDocumentTOResponse.getDeliveryTypeAtDestination());
                assertNotNull(transportDocumentTOResponse.getCargoMovementTypeAtOrigin());
                assertNotNull(transportDocumentTOResponse.getCargoMovementTypeAtDestination());
                assertNotNull(transportDocumentTOResponse.getServiceContractReference());
                assertNotNull(transportDocumentTOResponse.getTermsAndConditions());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName("Get transport document without shipping instruction should return an error")
    void testFindTransportDocumentWithoutShippingInstruction() {
      when(transportDocumentRepository.findLatestTransportDocumentByTransportDocumentReference(any()))
          .thenReturn(Mono.just(transportDocument));
      when(carrierRepository.findById((UUID) any())).thenReturn(Mono.just(carrier));
      when(locationService.fetchLocationDeepObjByID(shippingInstructionTO.getPlaceOfIssueID()))
          .thenReturn(Mono.just(locationTO));
      when(shippingInstructionService.findByID(transportDocument.getShippingInstructionID()))
          .thenReturn(Mono.empty());
      when(chargeService.fetchChargesByTransportDocumentID(transportDocument.getId()))
          .thenReturn(Flux.just(chargeTO));
      when(carrierClauseService.fetchCarrierClausesByTransportDocumentID(any()))
          .thenReturn(Flux.just(carrierClauseTO));
      when(shipmentLocationService.fetchShipmentLocationByTransportDocumentID(any())).thenReturn(Mono.empty());

      StepVerifier.create(
              transportDocumentServiceImpl.findByTransportDocumentReference(
                  "TransportDocumentReference1"))
          .expectErrorSatisfies(
              throwable -> {
                Assertions.assertTrue(throwable instanceof ConcreteRequestErrorMessageException);
                assertEquals(
                    "No shipping instruction found with shipping instruction reference: "
                        + transportDocument.getShippingInstructionID(),
                    throwable.getMessage());
              })
          .verify();
    }

    @Test
    @DisplayName(
        "Test transportDocument without issuer carrier should return transport document without issuer")
    void testGetTransportDocumentWithNoIssuerCarrierFound() {
      when(shipmentRepository.findByCarrierBookingReferenceAndValidUntilIsNull(any())).thenReturn(Mono.just(shipment));
      when(bookingRepository.findCarrierBookingReferenceAndValidUntilIsNull(any())).thenReturn(Mono.just(booking));
      when(transportDocumentRepository.findLatestTransportDocumentByTransportDocumentReference(any()))
          .thenReturn(Mono.just(transportDocument));
      when(carrierRepository.findById((UUID) any())).thenReturn(Mono.empty());
      when(locationService.fetchLocationDeepObjByID(shippingInstructionTO.getPlaceOfIssueID()))
          .thenReturn(Mono.just(locationTO));
      when(shippingInstructionService.findByID(transportDocument.getShippingInstructionID()))
          .thenReturn(Mono.just(shippingInstructionTO));
      when(chargeService.fetchChargesByTransportDocumentID(transportDocument.getId()))
          .thenReturn(Flux.just(chargeTO));
      when(carrierClauseService.fetchCarrierClausesByTransportDocumentID(any()))
          .thenReturn(Flux.just(carrierClauseTO));
      when(transportService.findByCarrierBookingReference(any())).thenReturn(Flux.just(transportTO));
      when(shipmentLocationService.fetchShipmentLocationByTransportDocumentID(any())).thenReturn(Mono.empty());

      StepVerifier.create(
              transportDocumentServiceImpl.findByTransportDocumentReference(
                  "TransportDocumentReference1"))
          .assertNext(
              transportDocumentTOResponse -> {
                assertNull(transportDocumentTOResponse.getIssuerCode());
                assertNull(transportDocumentTOResponse.getIssuerCodeListProvider());
                assertEquals(1, transportDocumentTOResponse.getCharges().size());
                assertNotNull(transportDocumentTOResponse.getPlaceOfIssue());
                assertEquals(1, transportDocumentTOResponse.getCarrierClauses().size());
                assertEquals(1, transportDocumentTOResponse.getTransports().size());
                assertNotNull(transportDocumentTOResponse.getReceiptTypeAtOrigin());
                assertNotNull(transportDocumentTOResponse.getDeliveryTypeAtDestination());
                assertNotNull(transportDocumentTOResponse.getCargoMovementTypeAtOrigin());
                assertNotNull(transportDocumentTOResponse.getCargoMovementTypeAtDestination());
                assertNotNull(transportDocumentTOResponse.getServiceContractReference());
                assertNotNull(transportDocumentTOResponse.getTermsAndConditions());

              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "No transport document found for transport document reference should return an empty result.")
    void testNoTransportDocumentFound() {
      when(transportDocumentRepository.findLatestTransportDocumentByTransportDocumentReference(any()))
          .thenReturn(Mono.empty());

      StepVerifier.create(
              transportDocumentServiceImpl.findByTransportDocumentReference(
                  "TransportDocumentReference1"))
        .expectErrorMatches(
          throwable ->
            throwable instanceof ConcreteRequestErrorMessageException
              && ((ConcreteRequestErrorMessageException) throwable).getReason().equals("notFound")
              && throwable.getMessage().equals("No transport document found with transport document reference: TransportDocumentReference1")
        ).verify();
    }

    @Test
    @DisplayName("Test set SMDG code as Issuer on Transport document")
    void testSetSMDGCodeOnTransportDocument() {
      Carrier carrier = new Carrier();
      carrier.setSmdgCode("123");
      TransportDocumentTO transportDocumentTO = new TransportDocumentTO();
      transportDocumentServiceImpl.setIssuerOnTransportDocument(transportDocumentTO, carrier);
      assertEquals(carrier.getSmdgCode(), transportDocumentTO.getIssuerCode());
      assertEquals(CarrierCodeListProvider.SMDG, transportDocumentTO.getIssuerCodeListProvider());
    }

    @Test
    @DisplayName("Test set NMFTA code as Issuer on Transport document")
    void testSetNMFTOCodeOnTransportDocument() {
      Carrier carrier = new Carrier();
      carrier.setNmftaCode("abcd");
      TransportDocumentTO transportDocumentTO = new TransportDocumentTO();
      transportDocumentServiceImpl.setIssuerOnTransportDocument(transportDocumentTO, carrier);
      assertEquals(carrier.getNmftaCode(), transportDocumentTO.getIssuerCode());
      assertEquals(CarrierCodeListProvider.NMFTA, transportDocumentTO.getIssuerCodeListProvider());
    }

    @Test
    @DisplayName("Test unable to set issuer on TransportDocument")
    void testNoIssuerOnTransportDocument() {
      Carrier carrier = new Carrier();
      TransportDocumentTO transportDocumentTO = new TransportDocumentTO();
      transportDocumentServiceImpl.setIssuerOnTransportDocument(transportDocumentTO, carrier);
      assertNull(transportDocumentTO.getIssuerCode());
    }
  }

  @Nested
  @DisplayName("Tests for the method ApproveTransportDocument(#CarrierBookingReference)")
  class ApproveTransportDocumentTOTest {

    @Test
    @DisplayName(
        "Approve at transport document with valid reference should return transport document with SI & bookings "
            + "document statuses set to APPR & CMPL respectively")
    void testApproveTransportDocument() {
      when(shipmentRepository.findByCarrierBookingReferenceAndValidUntilIsNull(any())).thenReturn(Mono.just(shipment));
      when(bookingRepository.findCarrierBookingReferenceAndValidUntilIsNull(any())).thenReturn(Mono.just(booking));
      when(transportDocumentRepository.findLatestTransportDocumentByTransportDocumentReference(any()))
          .thenReturn(Mono.just(transportDocument));
      when(carrierRepository.findById((UUID) any())).thenReturn(Mono.just(carrier));
      when(locationService.fetchLocationDeepObjByID(shippingInstructionTO.getPlaceOfIssueID()))
          .thenReturn(Mono.just(locationTO));
      when(shippingInstructionService.findByID(transportDocument.getShippingInstructionID()))
          .thenReturn(Mono.just(shippingInstructionTO));
      when(chargeService.fetchChargesByTransportDocumentID(transportDocument.getId()))
          .thenReturn(Flux.just(chargeTO));
      when(carrierClauseService.fetchCarrierClausesByTransportDocumentID(any()))
          .thenReturn(Flux.just(carrierClauseTO));
      when(shippingInstructionRepository.setDocumentStatusByReference(any(), any(), any()))
          .thenReturn(Mono.empty());
      when(bookingRepository.findAllByShippingInstructionReference(any()))
          .thenReturn(Flux.just(booking));
      when(bookingRepository.findByCarrierBookingRequestReferenceAndValidUntilIsNull(any()))
          .thenReturn(Mono.just(new Booking()));
      when(bookingRepository
              .updateDocumentStatusAndUpdatedDateTimeForCarrierBookingRequestReference(
                  any(), any(), any()))
          .thenReturn(Mono.empty());
      when(shipmentService.findByShippingInstructionReference(any()))
          .thenReturn((Mono.just(List.of(shipmentTO))));
      when(shipmentEventService.create(any())).thenReturn(Mono.just(new ShipmentEvent()));
      when(transportService.findByCarrierBookingReference(any())).thenReturn(Flux.empty());
      when(shipmentLocationService.fetchShipmentLocationByTransportDocumentID(any())).thenReturn(Mono.empty());

      StepVerifier.create(
              transportDocumentServiceImpl.approveTransportDocument("TransportDocumentReference1"))
          .assertNext(
              transportDocumentRefStatusTOResponse -> {
                assertEquals(
                  "TransportDocumentReference1",
                  transportDocumentRefStatusTOResponse.getTransportDocumentReference()
                );
                assertEquals(
                  ShipmentEventTypeCode.APPR,
                  transportDocumentRefStatusTOResponse.getDocumentStatus()
                );
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "No transport document found for transport document reference should raise a mono error")
    void testNoTransportDocumentFoundToApprove() {
      when(transportDocumentRepository.findLatestTransportDocumentByTransportDocumentReference(any()))
          .thenReturn(Mono.empty());

      StepVerifier.create(
              transportDocumentServiceImpl.approveTransportDocument("TransportDocumentReference1"))
          .expectErrorMatches(
              throwable ->
                  throwable instanceof ConcreteRequestErrorMessageException
                      && ((ConcreteRequestErrorMessageException) throwable)
                          .getReason()
                          .equals("notFound")
                      && throwable
                          .getMessage()
                          .equals(
                              "No transport document found with transport document reference: TransportDocumentReference1"))
          .verify();
    }

    @Test
    @DisplayName(
        "Approving a transport document that has a SI with invalid status document status  should raise a mono error")
    void testApproveTransportDocumentThatHasShippingInstructionInvalidDocumentStatus() {

      when(bookingRepository.findAllByShippingInstructionReference(any()))
          .thenReturn(Flux.just(booking));
      transportDocumentTO.getShippingInstruction().setDocumentStatus(ShipmentEventTypeCode.RECE);
      assertEquals(
          transportDocumentTO.getShippingInstruction().getDocumentStatus(),
          ShipmentEventTypeCode.RECE);

      when(shipmentRepository.findByCarrierBookingReferenceAndValidUntilIsNull(any())).thenReturn(Mono.just(shipment));
      when(bookingRepository.findCarrierBookingReferenceAndValidUntilIsNull(any())).thenReturn(Mono.just(booking));
      when(transportDocumentRepository.findLatestTransportDocumentByTransportDocumentReference(any()))
          .thenReturn(Mono.just(transportDocument));
      when(carrierRepository.findById((UUID) any())).thenReturn(Mono.just(carrier));
      when(locationService.fetchLocationDeepObjByID(shippingInstructionTO.getPlaceOfIssueID()))
          .thenReturn(Mono.just(locationTO));
      when(shippingInstructionService.findByID(transportDocument.getShippingInstructionID()))
          .thenReturn(Mono.just(shippingInstructionTO));
      when(chargeService.fetchChargesByTransportDocumentID(transportDocument.getId()))
          .thenReturn(Flux.just(chargeTO));
      when(carrierClauseService.fetchCarrierClausesByTransportDocumentID(any()))
          .thenReturn(Flux.just(carrierClauseTO));
      when(transportService.findByCarrierBookingReference(any())).thenReturn(Flux.empty());
      when(shipmentLocationService.fetchShipmentLocationByTransportDocumentID(any())).thenReturn(Mono.empty());

      StepVerifier.create(
              transportDocumentServiceImpl.approveTransportDocument("TransportDocumentReference1"))
          .expectErrorMatches(
              throwable ->
                  throwable instanceof ConcreteRequestErrorMessageException
                      && ((ConcreteRequestErrorMessageException) throwable)
                          .getReason()
                          .equals("invalidParameter")
                      && throwable
                          .getMessage()
                          .equals(
                              "Cannot Approve Transport Document with Shipping Instruction that is not in status DRFT"))
          .verify();
    }

    @Test
    @DisplayName(
        "Approving a transport document that has a SI with no shipments  should raise a mono error")
    void testApproveTransportDocumentThatHasShippingInstructionWithNoShipments() {

      when(shipmentRepository.findByCarrierBookingReferenceAndValidUntilIsNull(any())).thenReturn(Mono.just(shipment));
      when(bookingRepository.findCarrierBookingReferenceAndValidUntilIsNull(any())).thenReturn(Mono.just(booking));
      when(transportDocumentRepository.findLatestTransportDocumentByTransportDocumentReference(any()))
          .thenReturn(Mono.just(transportDocument));
      when(carrierRepository.findById((UUID) any())).thenReturn(Mono.just(carrier));
      when(locationService.fetchLocationDeepObjByID(shippingInstructionTO.getPlaceOfIssueID()))
          .thenReturn(Mono.just(locationTO));
      when(shippingInstructionService.findByID(transportDocument.getShippingInstructionID()))
          .thenReturn(Mono.just(shippingInstructionTO));
      when(chargeService.fetchChargesByTransportDocumentID(transportDocument.getId()))
          .thenReturn(Flux.just(chargeTO));
      when(carrierClauseService.fetchCarrierClausesByTransportDocumentID(any()))
          .thenReturn(Flux.just(carrierClauseTO));
      when(shippingInstructionRepository.setDocumentStatusByReference(any(), any(), any()))
          .thenReturn(Mono.empty());
      when(shipmentService.findByShippingInstructionReference(any()))
          .thenReturn((Mono.just(Collections.emptyList())));
      when(bookingRepository.findAllByShippingInstructionReference(any()))
          .thenReturn(Flux.just(booking));
      when(transportService.findByCarrierBookingReference(any())).thenReturn(Flux.empty());
      when(shipmentLocationService.fetchShipmentLocationByTransportDocumentID(any())).thenReturn(Mono.empty());

      StepVerifier.create(
              transportDocumentServiceImpl.approveTransportDocument("TransportDocumentReference1"))
          .expectErrorMatches(
              throwable ->
                  throwable instanceof ConcreteRequestErrorMessageException
                      && ((ConcreteRequestErrorMessageException) throwable)
                          .getReason()
                          .equals("notFound")
                      && throwable
                          .getMessage()
                          .equals(
                              "No shipments found for Shipping instruction of transport document reference: "
                                  + transportDocumentTO.getTransportDocumentReference()))
          .verify();
    }

    @Test
    @DisplayName(
        "Approving a transport document that has a SI with no booking in any of the shipments should raise a mono error")
    void testApproveTransportDocumentThatHasShippingInstructionWithNoBookingInShipment() {

      when(shipmentRepository.findByCarrierBookingReferenceAndValidUntilIsNull(any())).thenReturn(Mono.just(shipment));
      when(bookingRepository.findCarrierBookingReferenceAndValidUntilIsNull(any())).thenReturn(Mono.just(booking));
      when(transportDocumentRepository.findLatestTransportDocumentByTransportDocumentReference(any()))
          .thenReturn(Mono.just(transportDocument));
      when(carrierRepository.findById((UUID) any())).thenReturn(Mono.just(carrier));
      when(locationService.fetchLocationDeepObjByID(shippingInstructionTO.getPlaceOfIssueID()))
          .thenReturn(Mono.just(locationTO));
      when(shippingInstructionService.findByID(transportDocument.getShippingInstructionID()))
          .thenReturn(Mono.just(shippingInstructionTO));
      when(chargeService.fetchChargesByTransportDocumentID(transportDocument.getId()))
          .thenReturn(Flux.just(chargeTO));
      when(carrierClauseService.fetchCarrierClausesByTransportDocumentID(any()))
          .thenReturn(Flux.just(carrierClauseTO));
      when(shippingInstructionRepository.setDocumentStatusByReference(any(), any(), any()))
          .thenReturn(Mono.empty());
      when(shipmentService.findByShippingInstructionReference(any()))
          .thenReturn((Mono.just(List.of(shipmentTO))));
      when(bookingRepository.findByCarrierBookingRequestReferenceAndValidUntilIsNull(any())).thenReturn(Mono.empty());
      when(bookingRepository.findAllByShippingInstructionReference(any()))
          .thenReturn(Flux.just(booking));
      when(transportService.findByCarrierBookingReference(any())).thenReturn(Flux.empty());
      when(shipmentLocationService.fetchShipmentLocationByTransportDocumentID(any())).thenReturn(Mono.empty());

      StepVerifier.create(
              transportDocumentServiceImpl.approveTransportDocument("TransportDocumentReference1"))
          .expectErrorMatches(
              throwable ->
                  throwable instanceof IllegalStateException
                      && throwable
                          .getMessage()
                          .equals(
                              "The CarrierBookingRequestReference: "
                                  + transportDocumentTO.getTransportDocumentReference()
                                  + " specified on ShippingInstruction:"
                                  + transportDocumentTO
                                      .getShippingInstruction()
                                      .getShippingInstructionReference()
                                  + " does not exist!"))
          .verify();
    }

    @Test
    @DisplayName("Fail if carrierBookingReference linked to a Booking with invalid documentStatus")
    void testCreateShippingInstructionShouldFailWithInvalidBookingDocumentStatus() {

      String transportDocumentReference = "TransportDocumentReference1";

      when(shipmentRepository.findByCarrierBookingReferenceAndValidUntilIsNull(any())).thenReturn(Mono.just(shipment));
      when(bookingRepository.findCarrierBookingReferenceAndValidUntilIsNull(any())).thenReturn(Mono.just(booking));
      when(transportDocumentRepository.findLatestTransportDocumentByTransportDocumentReference(any()))
          .thenReturn(Mono.just(transportDocument));
      when(carrierRepository.findById((UUID) any())).thenReturn(Mono.just(carrier));
      when(locationService.fetchLocationDeepObjByID(shippingInstructionTO.getPlaceOfIssueID()))
          .thenReturn(Mono.just(locationTO));
      when(shippingInstructionService.findByID(transportDocument.getShippingInstructionID()))
          .thenReturn(Mono.just(shippingInstructionTO));
      when(chargeService.fetchChargesByTransportDocumentID(transportDocument.getId()))
          .thenReturn(Flux.just(chargeTO));
      when(carrierClauseService.fetchCarrierClausesByTransportDocumentID(any()))
          .thenReturn(Flux.just(carrierClauseTO));
      when(transportService.findByCarrierBookingReference(any())).thenReturn(Flux.empty());
      when(shipmentLocationService.fetchShipmentLocationByTransportDocumentID(any())).thenReturn(Mono.empty());

      // Test all invalid status except CONFIRMED
      for (ShipmentEventTypeCode s : ShipmentEventTypeCode.values()) {
        if (!BOOKING_DOCUMENT_STATUSES.contains(s.toString())) continue;
        if (s.equals(ShipmentEventTypeCode.CONF)) continue;

        booking.setDocumentStatus(s);
        when(bookingRepository.findAllByShippingInstructionReference(any()))
            .thenReturn(Flux.just(booking));

        StepVerifier.create(
                transportDocumentServiceImpl.approveTransportDocument(
                    "TransportDocumentReference1"))
            .expectErrorSatisfies(
                throwable -> {
                  Assertions.assertTrue(throwable instanceof ConcreteRequestErrorMessageException);
                  assertEquals(
                      "DocumentStatus "
                          + booking.getDocumentStatus()
                          + " for booking "
                          + booking.getCarrierBookingRequestReference()
                          + " related to carrier booking reference "
                          + transportDocumentReference
                          + " is not in CONF state!",
                      throwable.getMessage());
                })
            .verify();
      }
    }

    @Test
    @DisplayName("Fail if carrierBookingReference is not linked to a Booking")
    void testCreateShippingInstructionShouldFailWithNoBookingFound() {

      String transportDocumentReference = "TransportDocumentReference1";

      when(shipmentRepository.findByCarrierBookingReferenceAndValidUntilIsNull(any())).thenReturn(Mono.just(shipment));
      when(bookingRepository.findCarrierBookingReferenceAndValidUntilIsNull(any())).thenReturn(Mono.just(booking));
      when(transportDocumentRepository.findLatestTransportDocumentByTransportDocumentReference(any()))
          .thenReturn(Mono.just(transportDocument));
      when(carrierRepository.findById((UUID) any())).thenReturn(Mono.just(carrier));
      when(locationService.fetchLocationDeepObjByID(shippingInstructionTO.getPlaceOfIssueID()))
          .thenReturn(Mono.just(locationTO));
      when(shippingInstructionService.findByID(transportDocument.getShippingInstructionID()))
          .thenReturn(Mono.just(shippingInstructionTO));
      when(chargeService.fetchChargesByTransportDocumentID(transportDocument.getId()))
          .thenReturn(Flux.just(chargeTO));
      when(carrierClauseService.fetchCarrierClausesByTransportDocumentID(any()))
          .thenReturn(Flux.just(carrierClauseTO));
      when(bookingRepository.findAllByShippingInstructionReference(any())).thenReturn(Flux.empty());
      when(transportService.findByCarrierBookingReference(any())).thenReturn(Flux.empty());
      when(shipmentLocationService.fetchShipmentLocationByTransportDocumentID(any())).thenReturn(Mono.empty());

      StepVerifier.create(
              transportDocumentServiceImpl.approveTransportDocument(transportDocumentReference))
          .expectErrorSatisfies(
              throwable -> {
                Assertions.assertTrue(throwable instanceof ConcreteRequestErrorMessageException);
                assertEquals(
                    "No booking found for carrier booking reference: " + transportDocumentReference,
                    throwable.getMessage());
              })
          .verify();
    }
  }
}
