package org.dcsa.ebl.service.impl;

import org.dcsa.core.events.model.*;
import org.dcsa.core.events.model.enums.*;
import org.dcsa.core.events.model.mapper.*;
import org.dcsa.core.events.model.mappers.LocationMapper;
import org.dcsa.core.events.model.transferobjects.*;
import org.dcsa.core.events.repository.ShipmentRepository;
import org.dcsa.core.events.service.*;
import org.dcsa.core.exception.ConcreteRequestErrorMessageException;
import org.dcsa.ebl.model.ShippingInstruction;
import org.dcsa.ebl.model.mappers.ShippingInstructionMapper;
import org.dcsa.ebl.model.transferobjects.ShippingInstructionResponseTO;
import org.dcsa.ebl.model.transferobjects.ShippingInstructionTO;
import org.dcsa.ebl.repository.ShippingInstructionRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests for ShippingInstruction Implementation.")
class ShippingInstructionServiceImplTest {

  @Mock ShippingInstructionRepository shippingInstructionRepository;
  @Mock ShipmentRepository shipmentRepository;

  @Mock LocationService locationService;
  @Mock ReferenceService referenceService;
  @Mock ShipmentEquipmentService ShipmentEquipmentService;
  @Mock ShipmentEventService shipmentEventService;
  @Mock DocumentPartyService documentPartyService;

  @InjectMocks ShippingInstructionServiceImpl shippingInstructionServiceImpl;

  @Spy
  ShippingInstructionMapper shippingInstructionMapper =
      Mappers.getMapper(ShippingInstructionMapper.class);

  @Spy LocationMapper locationMapper = Mappers.getMapper(LocationMapper.class);
  @Spy SealMapper sealMapper = Mappers.getMapper(SealMapper.class);
  @Spy CargoLineItemMapper cargoLineItemMapper = Mappers.getMapper(CargoLineItemMapper.class);
  @Spy CargoItemMapper cargoItemMapper = Mappers.getMapper(CargoItemMapper.class);

  @Spy
  ActiveReeferSettingsMapper activeReeferSettingsMapper =
      Mappers.getMapper(ActiveReeferSettingsMapper.class);

  @Spy EquipmentMapper equipmentMapper = Mappers.getMapper(EquipmentMapper.class);

  @Spy
  ShipmentEquipmentMapper shipmentEquipmentMapper =
      Mappers.getMapper(ShipmentEquipmentMapper.class);

  @Spy PartyMapper partyMapper = Mappers.getMapper(PartyMapper.class);

  ShippingInstruction shippingInstruction;
  Location location;
  Address address;
  Facility facility;
  Reference reference;
  DocumentParty documentParty;
  Party party;
  PartyIdentifyingCode partyIdentifyingCode;
  DisplayedAddress displayedAddress;
  ModeOfTransport modeOfTransport;
  Shipment shipment;
  ShipmentEvent shipmentEvent;

  ShippingInstructionTO shippingInstructionTO;
  LocationTO locationTO;
  ShippingInstructionResponseTO shippingInstructionResponseTO;
  ShipmentEquipmentTO shipmentEquipmentTO;
  ActiveReeferSettingsTO activeReeferSettingsTO;
  EquipmentTO equipmentTO;
  SealTO sealsTO;
  CargoLineItemTO cargoLineItemTO;
  CargoItemTO cargoItemTO;
  DocumentPartyTO documentPartyTO;
  ReferenceTO referenceTO;

  @BeforeEach
  void init() {
    initEntities();
    initTO();
  }

  private void initEntities() {
    location = new Location();
    location.setId("c703277f-84ca-4816-9ccf-fad8e202d3b6");
    location.setLocationName("Hamburg");
    location.setAddressID(UUID.fromString("8fecc6d0-2a78-401d-948a-b9753f6b53d5"));
    location.setFacilityID(UUID.fromString("74dcf8e6-4ed4-439e-a935-ec183df73013"));

    address = new Address();
    address.setId(UUID.fromString("8fecc6d0-2a78-401d-948a-b9753f6b53d5"));
    address.setName("Fraz");
    address.setStreet("Kronprincessegade");
    address.setPostalCode("1306");
    address.setCity("København");
    address.setCountry("Denmark");

    reference = new Reference();
    reference.setReferenceValue("test");
    reference.setReferenceType(ReferenceTypeCode.FF);

    party = new Party();
    party.setId("a680fe72-503e-40b3-9cfc-dcadafdecf15");
    party.setPartyName("DCSA");
    party.setAddressID(address.getId());

    partyIdentifyingCode = new PartyIdentifyingCode();
    partyIdentifyingCode.setPartyID(party.getId());
    partyIdentifyingCode.setCodeListName("LCL");
    partyIdentifyingCode.setDcsaResponsibleAgencyCode(DCSAResponsibleAgencyCode.ISO);
    partyIdentifyingCode.setPartyCode("MSK");

    modeOfTransport = new ModeOfTransport();
    modeOfTransport.setId("1");
    modeOfTransport.setDescription("Transport of goods and/or persons is by sea.");
    modeOfTransport.setName("Maritime transport");
    modeOfTransport.setDcsaTransportType(DCSATransportType.VESSEL);

    shipment = new Shipment();
    shipment.setShipmentID(UUID.randomUUID());
    shipment.setCarrierBookingReference("XYZ12345");
    shipment.setBookingID(UUID.randomUUID());
    shipment.setCarrierID(UUID.randomUUID());
    shipment.setConfirmationDateTime(OffsetDateTime.now());

    documentParty = new DocumentParty();
    documentParty.setId(UUID.fromString("3d9542f8-c362-4fa5-8902-90e30d87f1d4"));
    documentParty.setPartyID("d04fb8c6-eb9c-474d-9cf7-86aa6bfcc2a2");
    documentParty.setPartyFunction(PartyFunction.DDS);
    documentParty.setIsToBeNotified(true);
    documentParty.setShipmentID(shipment.getShipmentID());

    shippingInstruction = new ShippingInstruction();
    shippingInstruction.setIsShippedOnboardType(true);
    shippingInstruction.setIsElectronic(true);
    shippingInstruction.setIsToOrder(true);
    shippingInstruction.setShippingInstructionID(UUID.randomUUID().toString());
    shippingInstruction.setDocumentStatus(ShipmentEventTypeCode.RECE);
    shippingInstruction.setPlaceOfIssueID(location.getId());
    shippingInstruction.setAreChargesDisplayedOnCopies(true);

    OffsetDateTime now = OffsetDateTime.now();
    shippingInstruction.setShippingInstructionCreatedDateTime(now);
    shippingInstruction.setShippingInstructionUpdatedDateTime(now);

    displayedAddress = new DisplayedAddress();
    displayedAddress.setDocumentPartyID(documentParty.getId());
    displayedAddress.setAddressLine("Javastraat");
    displayedAddress.setAddressLineNumber(1);

    shipmentEvent = new ShipmentEvent();
    shipmentEvent.setEventID(UUID.randomUUID());
    shipmentEvent.setShipmentEventTypeCode(
        ShipmentEventTypeCode.valueOf(shippingInstruction.getDocumentStatus().name()));
    shipmentEvent.setDocumentTypeCode(DocumentTypeCode.SHI);
    shipmentEvent.setEventClassifierCode(EventClassifierCode.ACT);
    shipmentEvent.setDocumentID(shippingInstruction.getShippingInstructionID());
    shipmentEvent.setEventCreatedDateTime(OffsetDateTime.now());
    shipmentEvent.setEventDateTime(shippingInstruction.getShippingInstructionUpdatedDateTime());
  }

  private void initTO() {
    locationTO = locationMapper.locationToDTO(location, address, facility);

    equipmentTO = new EquipmentTO();
    equipmentTO.setIsoEquipmentCode("22G2");
    equipmentTO.setEquipmentReference("APZU4812090");
    equipmentTO.setTareWeight(12.12f);
    equipmentTO.setWeightUnit(WeightUnit.KGM);

    sealsTO = new SealTO();
    sealsTO.setSealNumber("1".repeat(15));
    sealsTO.setSealType(SealTypeCode.BLT);
    sealsTO.setSealSource(SealSourceCode.SHI);

    cargoLineItemTO = new CargoLineItemTO();
    cargoLineItemTO.setCargoLineItemID("Some CargoLineItem ID");
    cargoLineItemTO.setShippingMarks("All sorts of remarks!");

    cargoItemTO = new CargoItemTO();
    cargoItemTO.setCargoLineItems(List.of(cargoLineItemTO));
    cargoItemTO.setHsCode("x".repeat(10));
    cargoItemTO.setDescriptionOfGoods("Some description of the goods!");
    cargoItemTO.setNumberOfPackages(2);
    cargoItemTO.setPackageCode("XYZ");

    activeReeferSettingsTO = new ActiveReeferSettingsTO();
    activeReeferSettingsTO.setTemperatureUnit(TemperatureUnit.CEL);
    activeReeferSettingsTO.setHumidityMax(65f);
    activeReeferSettingsTO.setHumidityMin(20f);
    activeReeferSettingsTO.setTemperatureMax(70f);
    activeReeferSettingsTO.setTemperatureMin(-10f);
    activeReeferSettingsTO.setVentilationMax(15f);
    activeReeferSettingsTO.setVentilationMin(5f);

    shipmentEquipmentTO = new ShipmentEquipmentTO();
    shipmentEquipmentTO.setEquipment(equipmentTO);
    shipmentEquipmentTO.setSeals(List.of(sealsTO));
    shipmentEquipmentTO.setCargoItems(List.of(cargoItemTO));
    shipmentEquipmentTO.setCargoGrossWeight(120f);
    shipmentEquipmentTO.setCargoGrossWeightUnit(WeightUnit.KGM);
    shipmentEquipmentTO.setActiveReeferSettings(activeReeferSettingsTO);
    shipmentEquipmentTO.setIsShipperOwned(true);

    documentPartyTO = new DocumentPartyTO();
    documentPartyTO.setParty(partyMapper.partyToDTO(party));

    referenceTO = new ReferenceTO();
    referenceTO.setReferenceType(reference.getReferenceType());
    referenceTO.setReferenceValue(reference.getReferenceValue());

    shippingInstructionTO = shippingInstructionMapper.shippingInstructionToDTO(shippingInstruction);
    shippingInstructionTO.setCarrierBookingReference("XYZ12345");
    shippingInstructionTO.setPlaceOfIssue(locationTO);
    shippingInstructionTO.setShipmentEquipments(List.of(shipmentEquipmentTO));
    shippingInstructionTO.setDocumentParties(List.of(documentPartyTO));
    shippingInstructionTO.setReferences(List.of(referenceTO));

    // Date & Time
    OffsetDateTime now = OffsetDateTime.now();
    shippingInstructionResponseTO = new ShippingInstructionResponseTO();
    shippingInstructionResponseTO.setShippingInstructionCreatedDateTime(now);
    shippingInstructionResponseTO.setShippingInstructionUpdatedDateTime(now);
  }

  @Nested
  @DisplayName("Tests for the method createShippingInstruction(#ShippingInstructionTO)")
  class CreateShippingInstructionTest {

    @Test
    @DisplayName("Method should save shipping instruction and return shipping response")
    void testCreateShippingInstructionWithEverything() {

      when(shippingInstructionRepository.save(any())).thenReturn(Mono.just(shippingInstruction));
      when(locationService.createLocationByTO(any(), any())).thenReturn(Mono.just(locationTO));
      when(shipmentRepository.findByCarrierBookingReference(any())).thenReturn(Mono.just(shipment));
      when(ShipmentEquipmentService.createShipmentEquipment(any(), any(), any()))
          .thenReturn(Mono.just(List.of(shipmentEquipmentTO)));
      when(documentPartyService.createDocumentPartiesByShippingInstructionID(any(), any()))
          .thenReturn(Mono.just(List.of(documentPartyTO)));
      when(referenceService.createReferencesByShippingInstructionIDAndTOs(any(), any()))
          .thenReturn(Mono.just(List.of(referenceTO)));
      when(shipmentEventService.create(any()))
          .thenAnswer(arguments -> Mono.just(arguments.getArguments()[0]));

      ArgumentCaptor<ShippingInstructionTO> argumentCaptor =
          ArgumentCaptor.forClass(ShippingInstructionTO.class);

      ArgumentCaptor<ShipmentEvent> argumentCaptorShipmentEvent =
          ArgumentCaptor.forClass(ShipmentEvent.class);

      StepVerifier.create(
              shippingInstructionServiceImpl.createShippingInstruction(shippingInstructionTO))
          .assertNext(
              b -> {
                verify(locationService).createLocationByTO(any(), any());
                verify(shipmentRepository).findByCarrierBookingReference(any());
                verify(ShipmentEquipmentService).createShipmentEquipment(any(), any(), any());
                verify(documentPartyService)
                    .createDocumentPartiesByShippingInstructionID(any(), any());
                verify(referenceService)
                    .createReferencesByShippingInstructionIDAndTOs(any(), any());

                verify(shipmentEventService, times(2))
                    .create(argumentCaptorShipmentEvent.capture());
                assertEquals(
                    "Received",
                    argumentCaptorShipmentEvent
                        .getAllValues()
                        .get(0)
                        .getShipmentEventTypeCode()
                        .getValue());
                assertEquals(
                    "Pending Confirmation",
                    argumentCaptorShipmentEvent
                        .getAllValues()
                        .get(1)
                        .getShipmentEventTypeCode()
                        .getValue());

                assertEquals(
                    shippingInstruction.getShippingInstructionID(), b.getShippingInstructionID());
                assertEquals("Pending Confirmation", b.getDocumentStatus().getValue());
                assertNotNull(b.getShippingInstructionCreatedDateTime());
                assertNotNull(b.getShippingInstructionUpdatedDateTime());

                verify(shippingInstructionMapper)
                    .dtoToShippingInstructionResponseTO(argumentCaptor.capture());
                assertEquals(
                    shippingInstruction.getShippingInstructionID(),
                    argumentCaptor.getValue().getShippingInstructionID());
                assertEquals(
                    shippingInstruction.getPlaceOfIssueID(),
                    argumentCaptor.getValue().getPlaceOfIssueID());
                assertEquals(
                    ShipmentEventTypeCode.PENC, argumentCaptor.getValue().getDocumentStatus());
                assertNotNull(argumentCaptor.getValue().getPlaceOfIssue());
                assertNotNull(argumentCaptor.getValue().getDocumentParties());
                assertNotNull(argumentCaptor.getValue().getReferences());
                assertNotNull(argumentCaptor.getValue().getShipmentEquipments());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Method should save shipping instruction and return shipping response when carrierBookingReference is set on cargoItem")
    void testCreateShippingInstructionWithCarrierBookingReferenceOnCargoItem() {
      shippingInstructionTO.setCarrierBookingReference(null);
      cargoItemTO.setCarrierBookingReference("carrierBookingRequestReference");

      when(shippingInstructionRepository.save(any())).thenReturn(Mono.just(shippingInstruction));
      when(locationService.createLocationByTO(any(), any())).thenReturn(Mono.just(locationTO));
      when(shipmentRepository.findByCarrierBookingReference(any())).thenReturn(Mono.just(shipment));
      when(ShipmentEquipmentService.createShipmentEquipment(any(), any(), any()))
          .thenReturn(Mono.just(List.of(shipmentEquipmentTO)));
      when(documentPartyService.createDocumentPartiesByShippingInstructionID(any(), any()))
          .thenReturn(Mono.just(List.of(documentPartyTO)));
      when(referenceService.createReferencesByShippingInstructionIDAndTOs(any(), any()))
          .thenReturn(Mono.just(List.of(referenceTO)));
      when(shipmentEventService.create(any()))
          .thenAnswer(arguments -> Mono.just(arguments.getArguments()[0]));

      ArgumentCaptor<ShippingInstructionTO> argumentCaptor =
          ArgumentCaptor.forClass(ShippingInstructionTO.class);

      ArgumentCaptor<ShipmentEvent> argumentCaptorShipmentEvent =
          ArgumentCaptor.forClass(ShipmentEvent.class);

      StepVerifier.create(
              shippingInstructionServiceImpl.createShippingInstruction(shippingInstructionTO))
          .assertNext(
              b -> {
                verify(locationService).createLocationByTO(any(), any());
                verify(shipmentRepository).findByCarrierBookingReference(any());
                verify(ShipmentEquipmentService).createShipmentEquipment(any(), any(), any());
                verify(documentPartyService)
                    .createDocumentPartiesByShippingInstructionID(any(), any());
                verify(referenceService)
                    .createReferencesByShippingInstructionIDAndTOs(any(), any());

                verify(shipmentEventService, times(2))
                    .create(argumentCaptorShipmentEvent.capture());
                assertEquals(
                    "Received",
                    argumentCaptorShipmentEvent
                        .getAllValues()
                        .get(0)
                        .getShipmentEventTypeCode()
                        .getValue());
                assertEquals(
                    "Pending Confirmation",
                    argumentCaptorShipmentEvent
                        .getAllValues()
                        .get(1)
                        .getShipmentEventTypeCode()
                        .getValue());

                assertEquals(
                    shippingInstruction.getShippingInstructionID(), b.getShippingInstructionID());
                assertEquals("Pending Confirmation", b.getDocumentStatus().getValue());
                assertNotNull(b.getShippingInstructionCreatedDateTime());
                assertNotNull(b.getShippingInstructionUpdatedDateTime());

                verify(shippingInstructionMapper)
                    .dtoToShippingInstructionResponseTO(argumentCaptor.capture());
                assertEquals(
                    shippingInstruction.getShippingInstructionID(),
                    argumentCaptor.getValue().getShippingInstructionID());
                assertEquals(
                    shippingInstruction.getPlaceOfIssueID(),
                    argumentCaptor.getValue().getPlaceOfIssueID());
                assertEquals(
                    ShipmentEventTypeCode.PENC, argumentCaptor.getValue().getDocumentStatus());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName("Method should save a shallow shipping instruction and return shipping response")
    void testCreateShippingInstructionShallow() {

      shippingInstructionTO.setPlaceOfIssue(null);
      shippingInstructionTO.setDocumentParties(null);
      shippingInstructionTO.setReferences(null);
      shippingInstructionTO.setShipmentEquipments(null);

      when(shippingInstructionRepository.save(any())).thenReturn(Mono.just(shippingInstruction));
      when(shipmentEventService.create(any()))
          .thenAnswer(arguments -> Mono.just(arguments.getArguments()[0]));

      ArgumentCaptor<ShippingInstructionTO> argumentCaptor =
          ArgumentCaptor.forClass(ShippingInstructionTO.class);

      ArgumentCaptor<ShipmentEvent> argumentCaptorShipmentEvent =
          ArgumentCaptor.forClass(ShipmentEvent.class);

      StepVerifier.create(
              shippingInstructionServiceImpl.createShippingInstruction(shippingInstructionTO))
          .assertNext(
              b -> {
                assertEquals(
                    shippingInstruction.getShippingInstructionID(), b.getShippingInstructionID());
                assertEquals("Pending Confirmation", b.getDocumentStatus().getValue());
                assertNotNull(b.getShippingInstructionCreatedDateTime());
                assertNotNull(b.getShippingInstructionUpdatedDateTime());

                verify(shipmentEventService, times(2))
                    .create(argumentCaptorShipmentEvent.capture());
                assertEquals(
                    "Received",
                    argumentCaptorShipmentEvent
                        .getAllValues()
                        .get(0)
                        .getShipmentEventTypeCode()
                        .getValue());
                assertEquals(
                    "Pending Confirmation",
                    argumentCaptorShipmentEvent
                        .getAllValues()
                        .get(1)
                        .getShipmentEventTypeCode()
                        .getValue());

                verify(shippingInstructionMapper)
                    .dtoToShippingInstructionResponseTO(argumentCaptor.capture());
                assertEquals(
                    shippingInstruction.getShippingInstructionID(),
                    argumentCaptor.getValue().getShippingInstructionID());
                assertEquals(
                    shippingInstruction.getPlaceOfIssueID(),
                    argumentCaptor.getValue().getPlaceOfIssueID());
                assertEquals(
                    ShipmentEventTypeCode.PENC, argumentCaptor.getValue().getDocumentStatus());

                verify(locationService, never()).createLocationByTO(any(), any());
                verify(shipmentRepository, never()).findByCarrierBookingReference(any());
                verify(ShipmentEquipmentService, never())
                    .createShipmentEquipment(any(), any(), any());
                verify(documentPartyService, never())
                    .createDocumentPartiesByShippingInstructionID(any(), any());
                verify(referenceService, never())
                    .createReferencesByShippingInstructionIDAndTOs(any(), any());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName("Method should save a shallow shipping with validation errors resulting in PENU")
    void testCreateShippingInstructionResultingInPENU() {

      shippingInstructionTO.setPlaceOfIssue(null);
      shippingInstructionTO.setDocumentParties(null);
      shippingInstructionTO.setReferences(null);
      shippingInstructionTO.setShipmentEquipments(null);
      shippingInstructionTO.setIsElectronic(false);
      shippingInstructionTO.setNumberOfCopies(null);

      when(shippingInstructionRepository.save(any())).thenReturn(Mono.just(shippingInstruction));
      when(shipmentEventService.create(any()))
          .thenAnswer(arguments -> Mono.just(arguments.getArguments()[0]));

      ArgumentCaptor<ShippingInstructionTO> argumentCaptor =
          ArgumentCaptor.forClass(ShippingInstructionTO.class);

      ArgumentCaptor<ShipmentEvent> argumentCaptorShipmentEvent =
          ArgumentCaptor.forClass(ShipmentEvent.class);

      StepVerifier.create(
              shippingInstructionServiceImpl.createShippingInstruction(shippingInstructionTO))
          .assertNext(
              b -> {
                assertEquals(
                    shippingInstruction.getShippingInstructionID(), b.getShippingInstructionID());
                assertEquals("Pending Update", b.getDocumentStatus().getValue());
                assertNotNull(b.getShippingInstructionCreatedDateTime());
                assertNotNull(b.getShippingInstructionUpdatedDateTime());

                verify(shipmentEventService, times(2))
                    .create(argumentCaptorShipmentEvent.capture());
                assertEquals(
                    "Received",
                    argumentCaptorShipmentEvent
                        .getAllValues()
                        .get(0)
                        .getShipmentEventTypeCode()
                        .getValue());
                assertEquals(
                    "Pending Update",
                    argumentCaptorShipmentEvent
                        .getAllValues()
                        .get(1)
                        .getShipmentEventTypeCode()
                        .getValue());

                verify(shippingInstructionMapper)
                    .dtoToShippingInstructionResponseTO(argumentCaptor.capture());
                assertEquals(
                    shippingInstruction.getShippingInstructionID(),
                    argumentCaptor.getValue().getShippingInstructionID());
                assertEquals(
                    shippingInstruction.getPlaceOfIssueID(),
                    argumentCaptor.getValue().getPlaceOfIssueID());
                assertEquals(
                    ShipmentEventTypeCode.PENU, argumentCaptor.getValue().getDocumentStatus());

                verify(locationService, never()).createLocationByTO(any(), any());
                verify(shipmentRepository, never()).findByCarrierBookingReference(any());
                verify(ShipmentEquipmentService, never())
                    .createShipmentEquipment(any(), any(), any());
                verify(documentPartyService, never())
                    .createDocumentPartiesByShippingInstructionID(any(), any());
                verify(referenceService, never())
                    .createReferencesByShippingInstructionIDAndTOs(any(), any());
                assertNull(argumentCaptor.getValue().getPlaceOfIssue());
                assertNull(argumentCaptor.getValue().getDocumentParties());
                assertNull(argumentCaptor.getValue().getReferences());
                assertNull(argumentCaptor.getValue().getShipmentEquipments());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName("Failing to create a shipment event should result in error")
    void testShipmentEventFailedShouldResultInError() {

      shippingInstructionTO.setPlaceOfIssue(null);
      shippingInstructionTO.setDocumentParties(null);
      shippingInstructionTO.setReferences(null);
      shippingInstructionTO.setShipmentEquipments(null);

      when(shippingInstructionRepository.save(any()))
          .thenAnswer(arguments -> Mono.just(shippingInstruction));
      when(shipmentEventService.create(any())).thenAnswer(arguments -> Mono.empty());

      StepVerifier.create(
              shippingInstructionServiceImpl.createShippingInstruction(shippingInstructionTO))
          .expectErrorSatisfies(
              throwable -> {
                Assertions.assertTrue(throwable instanceof ConcreteRequestErrorMessageException);
                assertEquals(
                    "Failed to create shipment event for ShippingInstruction.",
                    throwable.getMessage());
              })
          .verify();
    }

    @Test
    @DisplayName(
        "Fail if ShippingInstruction contains no carrierBookingReference on SI and ShipmentEquipment is null")
    void testCreateBookingShouldFailWithNoCarrierBookingReferenceAndNoShipmentEquipment() {

      shippingInstructionTO.setCarrierBookingReference(null);
      shippingInstructionTO.setShipmentEquipments(null);

      StepVerifier.create(
              shippingInstructionServiceImpl.createShippingInstruction(shippingInstructionTO))
          .expectErrorSatisfies(
              throwable -> {
                Assertions.assertTrue(throwable instanceof ConcreteRequestErrorMessageException);
                assertEquals(
                    "CarrierBookingReference needs to be defined on either ShippingInstruction or CargoItemTO level.",
                    throwable.getMessage());
              })
          .verify();
    }

    @Test
    @DisplayName(
        "Fail if ShippingInstruction does not contain any carrierBookingReference on any level")
    void testCreateBookingShouldFailWithNoCarrierBookingReference() {

      shippingInstructionTO.setCarrierBookingReference(null);
      cargoItemTO.setCarrierBookingReference(null);
      shipmentEquipmentTO.setCargoItems(List.of(cargoItemTO));
      shippingInstructionTO.setShipmentEquipments(List.of(shipmentEquipmentTO));

      StepVerifier.create(
              shippingInstructionServiceImpl.createShippingInstruction(shippingInstructionTO))
          .expectErrorSatisfies(
              throwable -> {
                Assertions.assertTrue(throwable instanceof ConcreteRequestErrorMessageException);
                assertEquals(
                    "CarrierBookingReference needs to be defined on either ShippingInstruction or CargoItemTO level.",
                    throwable.getMessage());
              })
          .verify();
    }

    @Test
    @DisplayName(
        "Fail if ShippingInstruction contains carrierBookingReference on both root and in CargoItems")
    void testCreateBookingShouldFailWithCarrierBookingReferenceInRootAndInCargoItem() {

      cargoItemTO.setCarrierBookingReference("CarrierBookingReference");

      StepVerifier.create(
              shippingInstructionServiceImpl.createShippingInstruction(shippingInstructionTO))
          .expectErrorSatisfies(
              throwable -> {
                Assertions.assertTrue(throwable instanceof ConcreteRequestErrorMessageException);
                assertEquals(
                    "CarrierBookingReference defined on both ShippingInstruction and CargoItemTO level.",
                    throwable.getMessage());
              })
          .verify();
    }
  }

  @Nested
  @DisplayName("Tests for the method validateShippingInstruction(#ShippingInstructionTO)")
  class ValidateShippingInstruction {

    @Test
    @DisplayName(
        "Test validateShipping Instruction with valid shipping instruction should result in no validation errors")
    void testValidShippingInstruction() {
      List<String> validationResult =
          shippingInstructionServiceImpl.validateShippingInstruction(shippingInstructionTO);
      assertEquals(0, validationResult.size());
    }

    @Test
    @DisplayName("Test validateShipping Instruction with invalid number of copies")
    void testInvalidNumberOfCopiesShippingInstruction() {
      shippingInstructionTO.setIsElectronic(false);
      shippingInstructionTO.setNumberOfCopies(null);
      shippingInstructionTO.setNumberOfOriginals(1);
      List<String> validationResult =
          shippingInstructionServiceImpl.validateShippingInstruction(shippingInstructionTO);
      assertEquals(1, validationResult.size());
      assertEquals(
          "number of copies is required for non electronic shipping instructions.",
          validationResult.get(0));
    }

    @Test
    @DisplayName("Test validateShippingInstruction with no carrierBookingReferences")
    void testWithoutCarrierBookingReferences() {
      shippingInstructionTO.setCarrierBookingReference(null);
      cargoItemTO.setCarrierBookingReference(null);
      List<String> validationResult =
          shippingInstructionServiceImpl.validateShippingInstruction(shippingInstructionTO);
      assertEquals(1, validationResult.size());
      assertEquals(
          "Carrier Booking Reference not present on shipping instruction.",
          validationResult.get(0));
    }

    @Test
    @DisplayName(
        "Test validateShippingInstruction with carrierBookingReference defined on both shipping instruction as well as cargoItems")
    void testWithDuplicateCarrierBookingReference() {
      shippingInstructionTO.setCarrierBookingReference("CarrierBookingReference");
      cargoItemTO.setCarrierBookingReference("CarrierBookingReference");
      List<String> validationResult =
          shippingInstructionServiceImpl.validateShippingInstruction(shippingInstructionTO);
      assertEquals(1, validationResult.size());
      assertEquals(
          "Carrier Booking Reference present in both shipping instruction as well as cargo items.",
          validationResult.get(0));
    }

    @Test
    @DisplayName("Test validateShippingInstruction with invalid number of originals")
    void testInvalidNumberOfOriginalsShippingInstruction() {
      shippingInstructionTO.setIsElectronic(false);
      shippingInstructionTO.setNumberOfOriginals(null);
      shippingInstructionTO.setNumberOfCopies(1);
      List<String> validationResult =
          shippingInstructionServiceImpl.validateShippingInstruction(shippingInstructionTO);
      assertEquals(1, validationResult.size());
      assertEquals(
          "number of originals is required for non electronic shipping instructions.",
          validationResult.get(0));
    }

    @Test
    @DisplayName("Test validateShippingInstruction with invalid equipment tare weight")
    void testInvalidEquipmentTareWeight() {
      shippingInstructionTO.setCarrierBookingReference("CarrierBookingRequestReference");
      shipmentEquipmentTO.setIsShipperOwned(true);
      shipmentEquipmentTO.getEquipment().setTareWeight(null);
      List<String> validationResult =
          shippingInstructionServiceImpl.validateShippingInstruction(shippingInstructionTO);
      assertEquals(1, validationResult.size());
      assertEquals(
          "equipment tare weight is required for shipper owned equipment.",
          validationResult.get(0));
    }

    @Test
    @DisplayName("Test validateShippingInstruction with multiple invalid fields")
    void TestInvalidShippingInstruction() {
      shipmentEquipmentTO.setIsShipperOwned(true);
      shipmentEquipmentTO.getEquipment().setTareWeight(null);
      shippingInstructionTO.setIsElectronic(false);
      shippingInstructionTO.setNumberOfOriginals(null);
      shippingInstructionTO.setNumberOfCopies(null);

      List<String> validationResult =
          shippingInstructionServiceImpl.validateShippingInstruction(shippingInstructionTO);
      assertEquals(3, validationResult.size());
      assertEquals(
          "number of copies is required for non electronic shipping instructions.",
          validationResult.get(0));
      assertEquals(
          "number of originals is required for non electronic shipping instructions.",
          validationResult.get(1));
      assertEquals(
          "equipment tare weight is required for shipper owned equipment.",
          validationResult.get(2));
    }
  }
}
