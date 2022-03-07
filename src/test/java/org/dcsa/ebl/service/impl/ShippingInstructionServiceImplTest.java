package org.dcsa.ebl.service.impl;

import org.dcsa.core.events.edocumentation.model.mapper.ShipmentMapper;
import org.dcsa.core.events.edocumentation.model.transferobject.ShipmentTO;
import org.dcsa.core.events.edocumentation.service.ShipmentService;
import org.dcsa.core.events.model.*;
import org.dcsa.core.events.model.enums.*;
import org.dcsa.core.events.model.mapper.PartyMapper;
import org.dcsa.core.events.model.mappers.LocationMapper;
import org.dcsa.core.events.model.transferobjects.*;
import org.dcsa.core.events.repository.BookingRepository;
import org.dcsa.core.events.repository.ShipmentRepository;
import org.dcsa.core.events.service.*;
import org.dcsa.core.exception.ConcreteRequestErrorMessageException;
import org.dcsa.ebl.model.mappers.ShippingInstructionMapper;
import org.dcsa.ebl.model.transferobjects.ShippingInstructionResponseTO;
import org.dcsa.ebl.repository.ShippingInstructionRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.dcsa.core.events.model.enums.ShipmentEventTypeCode.BOOKING_DOCUMENT_STATUSES;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests for ShippingInstruction Implementation.")
class ShippingInstructionServiceImplTest {

  @Mock ShippingInstructionRepository shippingInstructionRepository;
  @Mock ShipmentRepository shipmentRepository;
  @Mock BookingRepository bookingRepository;

  @Mock LocationService locationService;
  @Mock ReferenceService referenceService;
  @Mock ShipmentEquipmentService shipmentEquipmentService;
  @Mock ShipmentEventService shipmentEventService;
  @Mock DocumentPartyService documentPartyService;
  @Mock ShipmentService shipmentService;

  @InjectMocks ShippingInstructionServiceImpl shippingInstructionServiceImpl;

  @Spy
  ShippingInstructionMapper shippingInstructionMapper =
      Mappers.getMapper(ShippingInstructionMapper.class);

  @Spy LocationMapper locationMapper = Mappers.getMapper(LocationMapper.class);
  @Spy PartyMapper partyMapper = Mappers.getMapper(PartyMapper.class);
  @Spy ShipmentMapper shipmentMapper = Mappers.getMapper(ShipmentMapper.class);

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
  ShipmentEquipment shipmentEquipment;
  CargoItem cargoItem;
  Booking booking;

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
  ShipmentTO shipmentTO;

  @BeforeEach
  void init() {
    initEntities();
    initTO();
  }

  private void initEntities() {
    booking = new Booking();
    booking.setId(UUID.randomUUID());
    booking.setDocumentStatus(ShipmentEventTypeCode.CONF);
    booking.setCarrierBookingRequestReference(UUID.randomUUID().toString());

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
    documentParty.setPartyID(party.getId());
    documentParty.setPartyFunction(PartyFunction.DDS);
    documentParty.setIsToBeNotified(true);
    documentParty.setShipmentID(shipment.getShipmentID());

    shippingInstruction = new ShippingInstruction();
    shippingInstruction.setIsShippedOnboardType(true);
    shippingInstruction.setIsElectronic(true);
    shippingInstruction.setIsToOrder(true);
    shippingInstruction.setShippingInstructionReference(UUID.randomUUID().toString());
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

    Equipment equipment = new Equipment();
    equipment.setEquipmentReference("equipment reference");

    shipmentEquipment = new ShipmentEquipment();
    shipmentEquipment.setId(UUID.randomUUID());
    shipmentEquipment.setShipmentID(shipment.getShipmentID());
    shipmentEquipment.setIsShipperOwned(false);
    shipmentEquipment.setCargoGrossWeightUnit(WeightUnit.KGM);
    shipmentEquipment.setCargoGrossWeight(21f);
    shipmentEquipment.setEquipmentReference(equipment.getEquipmentReference());

    cargoItem = new CargoItem();
    cargoItem.setId(UUID.randomUUID());
    cargoItem.setHsCode("x".repeat(10));
    cargoItem.setDescriptionOfGoods("Some description of the goods!");
    cargoItem.setNumberOfPackages(2);
    cargoItem.setPackageCode("XYZ");
    cargoItem.setShipmentEquipmentID(shipmentEquipment.getId());
    cargoItem.setShippingInstructionReference(shippingInstruction.getShippingInstructionReference());

    shipmentEvent = new ShipmentEvent();
    shipmentEvent.setEventID(UUID.randomUUID());
    shipmentEvent.setShipmentEventTypeCode(
        ShipmentEventTypeCode.valueOf(shippingInstruction.getDocumentStatus().name()));
    shipmentEvent.setDocumentTypeCode(DocumentTypeCode.SHI);
    shipmentEvent.setEventClassifierCode(EventClassifierCode.ACT);
    shipmentEvent.setDocumentID(shippingInstruction.getShippingInstructionReference());
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
    documentPartyTO.setPartyFunction(PartyFunction.DDR);
    documentPartyTO.setDisplayedAddress(List.of("displayedAddress"));

    referenceTO = new ReferenceTO();
    referenceTO.setReferenceType(reference.getReferenceType());
    referenceTO.setReferenceValue(reference.getReferenceValue());

    shipmentTO = shipmentMapper.shipmentToDTO(shipment);
    shipmentTO.setTermsAndConditions("Fail Fast, Fail Early, Fail Often");

    shippingInstructionTO = shippingInstructionMapper.shippingInstructionToDTO(shippingInstruction);
    shippingInstructionTO.setCarrierBookingReference("XYZ12345");
    shippingInstructionTO.setPlaceOfIssue(locationTO);
    shippingInstructionTO.setShipmentEquipments(List.of(shipmentEquipmentTO));
    shippingInstructionTO.setDocumentParties(List.of(documentPartyTO));
    shippingInstructionTO.setReferences(List.of(referenceTO));
    shippingInstructionTO.setShipments(List.of(shipmentTO));

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

      when(bookingRepository.findAllByCarrierBookingReference(any()))
          .thenReturn(Flux.just(booking));
      when(shippingInstructionRepository.save(any())).thenReturn(Mono.just(shippingInstruction));
      when(locationService.createLocationByTO(any(), any())).thenReturn(Mono.just(locationTO));
      when(shipmentEquipmentService.addShipmentEquipmentToShippingInstruction(any(), any()))
          .thenReturn(Mono.just(List.of(shipmentEquipmentTO)));
      when(documentPartyService.createDocumentPartiesByShippingInstructionReference(any(), any()))
          .thenReturn(Mono.just(List.of(documentPartyTO)));
      when(referenceService.createReferencesByShippingInstructionReferenceAndTOs(any(), any()))
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
                verify(shipmentEquipmentService)
                    .addShipmentEquipmentToShippingInstruction(any(), any());
                verify(documentPartyService)
                    .createDocumentPartiesByShippingInstructionReference(any(), any());
                verify(referenceService)
                    .createReferencesByShippingInstructionReferenceAndTOs(any(), any());

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
                    shippingInstruction.getShippingInstructionReference(), b.getShippingInstructionReference());
                assertEquals("Pending Confirmation", b.getDocumentStatus().getValue());
                assertNotNull(b.getShippingInstructionCreatedDateTime());
                assertNotNull(b.getShippingInstructionUpdatedDateTime());

                verify(shippingInstructionMapper)
                    .dtoToShippingInstructionResponseTO(argumentCaptor.capture());
                assertEquals(
                    shippingInstruction.getShippingInstructionReference(),
                    argumentCaptor.getValue().getShippingInstructionReference());
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

      when(bookingRepository.findAllByCarrierBookingReference(any()))
          .thenReturn(Flux.just(booking));
      when(shippingInstructionRepository.save(any())).thenReturn(Mono.just(shippingInstruction));
      when(locationService.createLocationByTO(any(), any())).thenReturn(Mono.just(locationTO));
      when(documentPartyService.createDocumentPartiesByShippingInstructionReference(any(), any()))
          .thenReturn(Mono.just(List.of(documentPartyTO)));
      when(referenceService.createReferencesByShippingInstructionReferenceAndTOs(any(), any()))
          .thenReturn(Mono.just(List.of(referenceTO)));
      when(shipmentEventService.create(any()))
          .thenAnswer(arguments -> Mono.just(arguments.getArguments()[0]));
      when(shipmentEquipmentService.addShipmentEquipmentToShippingInstruction(any(), any()))
          .thenReturn(Mono.empty());

      ArgumentCaptor<ShippingInstructionTO> argumentCaptor =
          ArgumentCaptor.forClass(ShippingInstructionTO.class);

      ArgumentCaptor<ShipmentEvent> argumentCaptorShipmentEvent =
          ArgumentCaptor.forClass(ShipmentEvent.class);

      StepVerifier.create(
              shippingInstructionServiceImpl.createShippingInstruction(shippingInstructionTO))
          .assertNext(
              b -> {
                verify(locationService).createLocationByTO(any(), any());
                verify(shipmentEquipmentService)
                    .addShipmentEquipmentToShippingInstruction(any(), any());
                verify(documentPartyService)
                    .createDocumentPartiesByShippingInstructionReference(any(), any());
                verify(referenceService)
                    .createReferencesByShippingInstructionReferenceAndTOs(any(), any());

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
                    shippingInstruction.getShippingInstructionReference(), b.getShippingInstructionReference());
                assertEquals("Pending Confirmation", b.getDocumentStatus().getValue());
                assertNotNull(b.getShippingInstructionCreatedDateTime());
                assertNotNull(b.getShippingInstructionUpdatedDateTime());

                verify(shippingInstructionMapper)
                    .dtoToShippingInstructionResponseTO(argumentCaptor.capture());
                assertEquals(
                    shippingInstruction.getShippingInstructionReference(),
                    argumentCaptor.getValue().getShippingInstructionReference());
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

      when(bookingRepository.findAllByCarrierBookingReference(any()))
          .thenReturn(Flux.just(booking));
      when(referenceService.createReferencesByShippingInstructionReferenceAndTOs(any(), any()))
          .thenReturn(Mono.empty());
      when(shipmentEquipmentService.addShipmentEquipmentToShippingInstruction(any(), any()))
          .thenReturn(Mono.empty());
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
                    shippingInstruction.getShippingInstructionReference(), b.getShippingInstructionReference());
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
                    shippingInstruction.getShippingInstructionReference(),
                    argumentCaptor.getValue().getShippingInstructionReference());
                assertEquals(
                    shippingInstruction.getPlaceOfIssueID(),
                    argumentCaptor.getValue().getPlaceOfIssueID());
                assertEquals(
                    ShipmentEventTypeCode.PENC, argumentCaptor.getValue().getDocumentStatus());

                verify(locationService, never()).createLocationByTO(any(), any());
                verify(documentPartyService, never())
                    .createDocumentPartiesByShippingInstructionReference(any(), any());
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

      when(bookingRepository.findAllByCarrierBookingReference(any()))
          .thenReturn(Flux.just(booking));
      when(referenceService.createReferencesByShippingInstructionReferenceAndTOs(any(), any()))
          .thenReturn(Mono.empty());
      when(shipmentEquipmentService.addShipmentEquipmentToShippingInstruction(any(), any()))
          .thenReturn(Mono.empty());
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
                    shippingInstruction.getShippingInstructionReference(), b.getShippingInstructionReference());
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
                    shippingInstruction.getShippingInstructionReference(),
                    argumentCaptor.getValue().getShippingInstructionReference());
                assertEquals(
                    shippingInstruction.getPlaceOfIssueID(),
                    argumentCaptor.getValue().getPlaceOfIssueID());
                assertEquals(
                    ShipmentEventTypeCode.PENU, argumentCaptor.getValue().getDocumentStatus());

                verify(locationService, never()).createLocationByTO(any(), any());
                verify(shipmentRepository, never()).findByCarrierBookingReference(any());
                verify(documentPartyService, never())
                    .createDocumentPartiesByShippingInstructionReference(any(), any());
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

      when(bookingRepository.findAllByCarrierBookingReference(any()))
          .thenReturn(Flux.just(booking));
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
    @DisplayName("Fail if carrierBookingReference linked to a Booking with invalid documentStatus")
    void testCreateShippingInstructionShouldFailWithInvalidBookingDocumentStatus() {

      // Test all invalid status except CONFIRMED
      for (ShipmentEventTypeCode s : ShipmentEventTypeCode.values()) {
        if (!BOOKING_DOCUMENT_STATUSES.contains(s.toString())) continue;
        if (s.equals(ShipmentEventTypeCode.CONF)) continue;

        booking.setDocumentStatus(s);
        when(bookingRepository.findAllByCarrierBookingReference(any()))
            .thenReturn(Flux.just(booking));

        StepVerifier.create(
                shippingInstructionServiceImpl.createShippingInstruction(shippingInstructionTO))
            .expectErrorSatisfies(
                throwable -> {
                  Assertions.assertTrue(throwable instanceof ConcreteRequestErrorMessageException);
                  assertEquals(
                      "DocumentStatus "
                          + booking.getDocumentStatus()
                          + " for booking "
                          + booking.getCarrierBookingRequestReference()
                          + " related to carrier booking reference "
                          + cargoItemTO.getCarrierBookingReference()
                          + " is not in CONF state!",
                      throwable.getMessage());
                })
            .verify();
      }
    }

    @Test
    @DisplayName("Fail if carrierBookingReference is not linked to a Booking")
    void testCreateShippingInstructionShouldFailWithNoBookingFound() {

      when(bookingRepository.findAllByCarrierBookingReference(any())).thenReturn(Flux.empty());

      StepVerifier.create(
              shippingInstructionServiceImpl.createShippingInstruction(shippingInstructionTO))
          .expectErrorSatisfies(
              throwable -> {
                Assertions.assertTrue(throwable instanceof ConcreteRequestErrorMessageException);
                assertEquals(
                    "No bookings found for carrier booking reference: "
                        + cargoItemTO.getCarrierBookingReference(),
                    throwable.getMessage());
              })
          .verify();
    }

    @Test
    @DisplayName(
        "Fail if ShippingInstruction contains carrierBookingReference on both root and in CargoItems")
    void testCreateShippingInstructionShouldFailWithCarrierBookingReferenceInRootAndInCargoItem() {

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
  @DisplayName(
      "Tests for the method updateShippingInstructionByShippingInstructionReference(#ShippingInstructionTO)")
  class UpdateShippingInstructionTest {

    @Test
    @DisplayName("Method should update existing shipping instruction and return shipping response")
    void testUpdateShippingInstructionWithEverything() {

      shippingInstruction.setDocumentStatus(ShipmentEventTypeCode.PENU);

      // saves
      when(shippingInstructionRepository.save(any())).thenReturn(Mono.just(shippingInstruction));
      when(locationService.resolveLocationByTO(any(), any(), any()))
          .thenReturn(Mono.just(locationTO));
      when(shipmentEventService.create(any()))
          .thenAnswer(arguments -> Mono.just(arguments.getArguments()[0]));

      // finds
      when(bookingRepository.findAllByCarrierBookingReference(any()))
          .thenReturn(Flux.just(booking));
      when(shippingInstructionRepository.findById(any(String.class)))
          .thenReturn(Mono.just(shippingInstruction));

      // deletes
      when(shipmentEquipmentService.resolveShipmentEquipmentsForShippingInstructionReference(any(), any()))
          .thenReturn(Mono.empty());
      when(documentPartyService.resolveDocumentPartiesForShippingInstructionReference(any(), any()))
          .thenReturn(Mono.empty());
      when(referenceService.resolveReferencesForShippingInstructionReference(any(), any()))
          .thenReturn(Mono.empty());

      ArgumentCaptor<ShippingInstructionTO> argumentCaptor =
          ArgumentCaptor.forClass(ShippingInstructionTO.class);

      ArgumentCaptor<ShipmentEvent> argumentCaptorShipmentEvent =
          ArgumentCaptor.forClass(ShipmentEvent.class);

      StepVerifier.create(
              shippingInstructionServiceImpl.updateShippingInstructionByShippingInstructionReference(
                  shippingInstruction.getShippingInstructionReference(), shippingInstructionTO))
          .assertNext(
              b -> {
                verify(locationService).resolveLocationByTO(any(), any(), any());
                verify(shipmentEquipmentService)
                    .resolveShipmentEquipmentsForShippingInstructionReference(any(), any());
                verify(documentPartyService)
                    .resolveDocumentPartiesForShippingInstructionReference(any(), any());
                verify(referenceService).resolveReferencesForShippingInstructionReference(any(), any());

                verify(shipmentEventService, times(2))
                    .create(argumentCaptorShipmentEvent.capture());
                assertEquals(
                    ShipmentEventTypeCode.PENU.getValue(),
                    argumentCaptorShipmentEvent
                        .getAllValues()
                        .get(0)
                        .getShipmentEventTypeCode()
                        .getValue());
                assertEquals(
                    ShipmentEventTypeCode.PENC.getValue(),
                    argumentCaptorShipmentEvent
                        .getAllValues()
                        .get(1)
                        .getShipmentEventTypeCode()
                        .getValue());

                assertEquals(
                    shippingInstruction.getShippingInstructionReference(), b.getShippingInstructionReference());
                assertEquals("Pending Confirmation", b.getDocumentStatus().getValue());
                assertNotNull(b.getShippingInstructionCreatedDateTime());
                assertNotNull(b.getShippingInstructionUpdatedDateTime());

                verify(shippingInstructionMapper)
                    .dtoToShippingInstructionResponseTO(argumentCaptor.capture());
                assertEquals(
                    shippingInstruction.getShippingInstructionReference(),
                    argumentCaptor.getValue().getShippingInstructionReference());
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
        "Method should update an existing shallow shipping instruction and return shipping response")
    void testUpdateShippingInstructionShallow() {

      shippingInstructionTO.setPlaceOfIssue(null);
      shippingInstructionTO.setDocumentParties(null);
      shippingInstructionTO.setReferences(null);
      shippingInstructionTO.setShipmentEquipments(null);

      shippingInstruction.setDocumentStatus(ShipmentEventTypeCode.PENU);

      // saves
      when(shippingInstructionRepository.save(any())).thenReturn(Mono.just(shippingInstruction));
      when(locationService.resolveLocationByTO(any(), any(), any()))
          .thenReturn(Mono.just(locationTO));
      when(shipmentEventService.create(any()))
          .thenAnswer(arguments -> Mono.just(arguments.getArguments()[0]));

      // finds
      when(bookingRepository.findAllByCarrierBookingReference(any()))
          .thenReturn(Flux.just(booking));
      when(shippingInstructionRepository.findById(any(String.class)))
          .thenReturn(Mono.just(shippingInstruction));

      // deletes
      when(shipmentEquipmentService.resolveShipmentEquipmentsForShippingInstructionReference(any(), any()))
          .thenReturn(Mono.empty());
      when(documentPartyService.resolveDocumentPartiesForShippingInstructionReference(any(), any()))
          .thenReturn(Mono.empty());
      when(referenceService.resolveReferencesForShippingInstructionReference(any(), any()))
          .thenReturn(Mono.empty());

      ArgumentCaptor<ShippingInstructionTO> argumentCaptor =
          ArgumentCaptor.forClass(ShippingInstructionTO.class);

      ArgumentCaptor<ShipmentEvent> argumentCaptorShipmentEvent =
          ArgumentCaptor.forClass(ShipmentEvent.class);

      StepVerifier.create(
              shippingInstructionServiceImpl.updateShippingInstructionByShippingInstructionReference(
                  shippingInstruction.getShippingInstructionReference(), shippingInstructionTO))
          .assertNext(
              b -> {
                assertEquals(
                    shippingInstruction.getShippingInstructionReference(), b.getShippingInstructionReference());
                assertEquals("Pending Confirmation", b.getDocumentStatus().getValue());
                assertNotNull(b.getShippingInstructionCreatedDateTime());
                assertNotNull(b.getShippingInstructionUpdatedDateTime());

                verify(shipmentEventService, times(2))
                    .create(argumentCaptorShipmentEvent.capture());
                assertEquals(
                    ShipmentEventTypeCode.PENU.getValue(),
                    argumentCaptorShipmentEvent
                        .getAllValues()
                        .get(0)
                        .getShipmentEventTypeCode()
                        .getValue());
                assertEquals(
                    ShipmentEventTypeCode.PENC.getValue(),
                    argumentCaptorShipmentEvent
                        .getAllValues()
                        .get(1)
                        .getShipmentEventTypeCode()
                        .getValue());

                verify(shippingInstructionMapper)
                    .dtoToShippingInstructionResponseTO(argumentCaptor.capture());
                assertEquals(
                    shippingInstruction.getShippingInstructionReference(),
                    argumentCaptor.getValue().getShippingInstructionReference());
                assertEquals(
                    shippingInstruction.getPlaceOfIssueID(),
                    argumentCaptor.getValue().getPlaceOfIssueID());
                assertEquals(
                    ShipmentEventTypeCode.PENC, argumentCaptor.getValue().getDocumentStatus());

                verify(locationService, never()).createLocationByTO(any(), any());
                verify(shipmentRepository, never()).findByCarrierBookingReference(any());
                verify(shipmentEquipmentService, never())
                    .createShipmentEquipment(any(), any(), any());
                verify(documentPartyService, never())
                    .createDocumentPartiesByShippingInstructionReference(any(), any());
                verify(referenceService, never())
                    .createReferencesByShippingInstructionReferenceAndTOs(any(), any());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Method should update an existing shallow shipping with validation errors resulting in PENU")
    void testUpdateShippingInstructionResultingInPENU() {

      shippingInstruction.setDocumentStatus(ShipmentEventTypeCode.PENU);

      shippingInstructionTO.setPlaceOfIssue(null);
      shippingInstructionTO.setDocumentParties(null);
      shippingInstructionTO.setReferences(null);
      shippingInstructionTO.setShipmentEquipments(null);
      shippingInstructionTO.setIsElectronic(false);
      shippingInstructionTO.setNumberOfCopies(null);

      // saves
      when(shippingInstructionRepository.save(any())).thenReturn(Mono.just(shippingInstruction));
      when(locationService.resolveLocationByTO(any(), any(), any())).thenReturn(Mono.empty());
      when(shipmentEventService.create(any()))
          .thenAnswer(arguments -> Mono.just(arguments.getArguments()[0]));

      // finds
      when(bookingRepository.findAllByCarrierBookingReference(any()))
          .thenReturn(Flux.just(booking));
      when(shippingInstructionRepository.findById(any(String.class)))
          .thenReturn(Mono.just(shippingInstruction));

      // deletes
      when(shipmentEquipmentService.resolveShipmentEquipmentsForShippingInstructionReference(any(), any()))
          .thenReturn(Mono.empty());
      when(documentPartyService.resolveDocumentPartiesForShippingInstructionReference(any(), any()))
          .thenReturn(Mono.empty());
      when(referenceService.resolveReferencesForShippingInstructionReference(any(), any()))
          .thenReturn(Mono.empty());

      ArgumentCaptor<ShippingInstructionTO> argumentCaptor =
          ArgumentCaptor.forClass(ShippingInstructionTO.class);

      ArgumentCaptor<ShipmentEvent> argumentCaptorShipmentEvent =
          ArgumentCaptor.forClass(ShipmentEvent.class);

      StepVerifier.create(
              shippingInstructionServiceImpl.updateShippingInstructionByShippingInstructionReference(
                  shippingInstruction.getShippingInstructionReference(), shippingInstructionTO))
          .assertNext(
              b -> {
                assertEquals(
                    shippingInstruction.getShippingInstructionReference(), b.getShippingInstructionReference());
                assertEquals("Pending Update", b.getDocumentStatus().getValue());
                assertNotNull(b.getShippingInstructionCreatedDateTime());
                assertNotNull(b.getShippingInstructionUpdatedDateTime());

                verify(shipmentEventService, times(2))
                    .create(argumentCaptorShipmentEvent.capture());
                assertEquals(
                    ShipmentEventTypeCode.PENU.getValue(),
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
                    shippingInstruction.getShippingInstructionReference(),
                    argumentCaptor.getValue().getShippingInstructionReference());
                assertEquals(
                    shippingInstruction.getPlaceOfIssueID(),
                    argumentCaptor.getValue().getPlaceOfIssueID());
                assertEquals(
                    ShipmentEventTypeCode.PENU, argumentCaptor.getValue().getDocumentStatus());

                verify(locationService, never()).createLocationByTO(any(), any());
                verify(shipmentRepository, never()).findByCarrierBookingReference(any());
                verify(shipmentEquipmentService, never())
                    .createShipmentEquipment(any(), any(), any());
                verify(documentPartyService, never())
                    .createDocumentPartiesByShippingInstructionReference(any(), any());
                verify(referenceService, never())
                    .createReferencesByShippingInstructionReferenceAndTOs(any(), any());
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

      shippingInstruction.setDocumentStatus(ShipmentEventTypeCode.PENU);

      shippingInstructionTO.setPlaceOfIssue(null);
      shippingInstructionTO.setDocumentParties(null);
      shippingInstructionTO.setReferences(null);
      shippingInstructionTO.setShipmentEquipments(null);

      when(shipmentEventService.create(any())).thenAnswer(arguments -> Mono.empty());
      when(bookingRepository.findAllByCarrierBookingReference(any()))
          .thenReturn(Flux.just(booking));
      when(shippingInstructionRepository.findById(any(String.class)))
          .thenReturn(Mono.just(shippingInstruction));

      StepVerifier.create(
              shippingInstructionServiceImpl.updateShippingInstructionByShippingInstructionReference(
                  shippingInstruction.getShippingInstructionReference(), shippingInstructionTO))
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
    @DisplayName("Fail if ShippingInstruction reference does not exist")
    void testUpdateBookingShouldFailWithNoCarrierBookingReferenceAndNoShipmentEquipment() {

      shippingInstructionTO.setShipmentEquipments(null);

      when(shippingInstructionRepository.findById(any(String.class))).thenReturn(Mono.empty());
      when(bookingRepository.findAllByCarrierBookingReference(any()))
          .thenReturn(Flux.just(booking));

      StepVerifier.create(
              shippingInstructionServiceImpl.updateShippingInstructionByShippingInstructionReference(
                  shippingInstruction.getShippingInstructionReference(), shippingInstructionTO))
          .expectErrorSatisfies(
              throwable -> {
                Assertions.assertTrue(throwable instanceof ConcreteRequestErrorMessageException);
                assertEquals(
                    "No Shipping Instruction found with ID: "
                        + shippingInstruction.getShippingInstructionReference(),
                    throwable.getMessage());
              })
          .verify();
    }

    @Test
    @DisplayName(
        "Fail if ShippingInstruction contains carrierBookingReference on both root and in CargoItems")
    void testUpdateBookingShouldFailWithCarrierBookingReferenceInRootAndInCargoItem() {

      cargoItemTO.setCarrierBookingReference("CarrierBookingReference");

      StepVerifier.create(
              shippingInstructionServiceImpl.updateShippingInstructionByShippingInstructionReference(
                  shippingInstruction.getShippingInstructionReference(), shippingInstructionTO))
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
  class ValidShippingInstructionTest {

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

  @Nested
  @DisplayName("Tests for the method findById(#ShippingInstructionReference)")
  class GetShippingInstructionTest {

    @Test
    @DisplayName("Test GET shipping instruction for an assumed valid ID.")
    void testGetShippingInstructionForInvalidID() {

      String invalidShippingInstructionReference = UUID.randomUUID().toString();

      when(shippingInstructionRepository.findById(any(String.class))).thenReturn(Mono.empty());

      StepVerifier.create(shippingInstructionServiceImpl.findById(invalidShippingInstructionReference))
          .expectErrorSatisfies(
              throwable -> {
                Assertions.assertTrue(throwable instanceof ConcreteRequestErrorMessageException);
                assertEquals(
                    "No Shipping Instruction found with ID: " + invalidShippingInstructionReference,
                    throwable.getMessage());
              })
          .verify();
    }

    @Test
    @DisplayName("Test GET shipping instruction for an assumed valid ID.")
    void testGetShippingInstructionForValidID() {
      String stubbedCRef = UUID.randomUUID().toString();
      when(shippingInstructionRepository.findById(any(String.class)))
          .thenReturn(Mono.just(shippingInstruction));
      when(shippingInstructionRepository.findCarrierBookingReferenceByShippingInstructionReference(any()))
          .thenReturn(Flux.just(stubbedCRef));
      when(locationService.fetchLocationByID(any())).thenReturn(Mono.just(locationTO));
      UUID sID1 = UUID.randomUUID();
      UUID sID2 = UUID.randomUUID();
      when(shippingInstructionRepository.findShipmentIDsByShippingInstructionReference(any()))
          .thenReturn(Flux.just(sID1, sID2));
      when(shipmentEquipmentService.findShipmentEquipmentByShipmentID(sID1))
          .thenReturn(Mono.just(Collections.singletonList(shipmentEquipmentTO)));
      when(shipmentEquipmentService.findShipmentEquipmentByShipmentID(sID2))
          .thenReturn(Mono.just(Collections.singletonList(shipmentEquipmentTO)));
      when(documentPartyService.fetchDocumentPartiesByByShippingInstructionReference(any()))
          .thenReturn(Mono.just(Collections.singletonList(documentPartyTO)));
      when(referenceService.findByShippingInstructionReference(any()))
          .thenReturn(Mono.just(Collections.singletonList(referenceTO)));
      when(shipmentService.findByShippingInstructionReference(any()))
          .thenReturn(Mono.just(Collections.singletonList(shipmentTO)));

      StepVerifier.create(shippingInstructionServiceImpl.findById(UUID.randomUUID().toString()))
          .assertNext(
              result -> {
                assertEquals(stubbedCRef, result.getCarrierBookingReference());
                assertEquals("Hamburg", result.getPlaceOfIssue().getLocationName());
                assertEquals(2, result.getShipmentEquipments().size());
                assertEquals(
                    "APZU4812090",
                    result.getShipmentEquipments().get(0).getEquipment().getEquipmentReference());
                assertEquals("DCSA", result.getDocumentParties().get(0).getParty().getPartyName());
                assertEquals(
                    ReferenceTypeCode.FF, result.getReferences().get(0).getReferenceType());
                assertEquals(
                    "Fail Fast, Fail Early, Fail Often",
                    result.getShipments().get(0).getTermsAndConditions());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName("Fail if carrierBookingReference linked to a Booking with invalid documentStatus")
    void testCreateShippingInstructionShouldFailWithInvalidBookingDocumentStatus() {

      // Test all invalid status except CONFIRMED
      for (ShipmentEventTypeCode s : ShipmentEventTypeCode.values()) {
        if (!BOOKING_DOCUMENT_STATUSES.contains(s.toString())) continue;
        if (s.equals(ShipmentEventTypeCode.CONF)) continue;

        booking.setDocumentStatus(s);
        when(bookingRepository.findAllByCarrierBookingReference(any()))
            .thenReturn(Flux.just(booking));

        StepVerifier.create(
                shippingInstructionServiceImpl.createShippingInstruction(shippingInstructionTO))
            .expectErrorSatisfies(
                throwable -> {
                  Assertions.assertTrue(throwable instanceof ConcreteRequestErrorMessageException);
                  assertEquals(
                      "DocumentStatus "
                          + booking.getDocumentStatus()
                          + " for booking "
                          + booking.getCarrierBookingRequestReference()
                          + " related to carrier booking reference "
                          + cargoItemTO.getCarrierBookingReference()
                          + " is not in CONF state!",
                      throwable.getMessage());
                })
            .verify();
      }
    }

    @Test
    @DisplayName("Fail if carrierBookingReference is not linked to a Booking")
    void testCreateShippingInstructionShouldFailWithNoBookingFound() {

      when(bookingRepository.findAllByCarrierBookingReference(any())).thenReturn(Flux.empty());

      StepVerifier.create(
              shippingInstructionServiceImpl.createShippingInstruction(shippingInstructionTO))
          .expectErrorSatisfies(
              throwable -> {
                Assertions.assertTrue(throwable instanceof ConcreteRequestErrorMessageException);
                assertEquals(
                    "No bookings found for carrier booking reference: "
                        + cargoItemTO.getCarrierBookingReference(),
                    throwable.getMessage());
              })
          .verify();
    }
  }
}
