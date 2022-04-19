package org.dcsa.ebl.service.impl;

import org.dcsa.core.events.edocumentation.model.mapper.ShipmentMapper;
import org.dcsa.core.events.edocumentation.model.transferobject.ConsignmentItemTO;
import org.dcsa.core.events.edocumentation.model.transferobject.ShipmentTO;
import org.dcsa.core.events.edocumentation.service.ConsignmentItemService;
import org.dcsa.core.events.edocumentation.service.ShipmentService;
import org.dcsa.core.events.model.*;
import org.dcsa.core.events.model.enums.*;
import org.dcsa.core.events.model.mapper.PartyMapper;
import org.dcsa.core.events.model.mapper.LocationMapper;
import org.dcsa.core.events.model.transferobjects.*;
import org.dcsa.core.events.repository.BookingRepository;
import org.dcsa.core.events.repository.ShipmentRepository;
import org.dcsa.core.events.repository.TransportDocumentRepository;
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

import static org.dcsa.core.events.model.enums.ShipmentEventTypeCode.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests for ShippingInstruction Implementation.")
class ShippingInstructionServiceImplTest {

  @Mock ShippingInstructionRepository shippingInstructionRepository;
  @Mock ShipmentRepository shipmentRepository;
  @Mock BookingRepository bookingRepository;
  @Mock TransportDocumentRepository transportDocumentRepository;

  @Mock LocationService locationService;
  @Mock ReferenceService referenceService;
  @Mock UtilizedTransportEquipmentService utilizedTransportEquipmentService;
  @Mock ShipmentEventService shipmentEventService;
  @Mock DocumentPartyService documentPartyService;
  @Mock ShipmentService shipmentService;
  @Mock ConsignmentItemService consignmentItemService;

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
  UtilizedTransportEquipment utilizedTransportEquipment;
  CargoItem cargoItem;
  Booking booking;

  ShippingInstructionTO shippingInstructionTO;
  LocationTO locationTO;
  ShippingInstructionResponseTO shippingInstructionResponseTO;
  UtilizedTransportEquipmentTO utilizedTransportEquipmentTO;
  ActiveReeferSettingsTO activeReeferSettingsTO;
  EquipmentTO equipmentTO;
  SealTO sealsTO;
  CargoLineItemTO cargoLineItemTO;
  CargoItemTO cargoItemTO;
  ConsignmentItemTO consignmentItemTO;
  DocumentPartyTO documentPartyTO1;
  DocumentPartyTO documentPartyTO2;
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
    shippingInstruction.setId(UUID.randomUUID());
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

    utilizedTransportEquipment = new UtilizedTransportEquipment();
    utilizedTransportEquipment.setId(UUID.randomUUID());
    utilizedTransportEquipment.setIsShipperOwned(false);
    utilizedTransportEquipment.setCargoGrossWeightUnit(WeightUnit.KGM);
    utilizedTransportEquipment.setCargoGrossWeight(21f);
    utilizedTransportEquipment.setEquipmentReference(equipment.getEquipmentReference());

    cargoItem = new CargoItem();
    cargoItem.setId(UUID.randomUUID());
    cargoItem.setNumberOfPackages(2);
    cargoItem.setPackageCode("XYZ");
    cargoItem.setUtilizedTransportEquipmentID(utilizedTransportEquipment.getId());
    cargoItem.setShippingInstructionID(shippingInstruction.getId());

    shipmentEvent = new ShipmentEvent();
    shipmentEvent.setEventID(UUID.randomUUID());
    shipmentEvent.setShipmentEventTypeCode(
        ShipmentEventTypeCode.valueOf(shippingInstruction.getDocumentStatus().name()));
    shipmentEvent.setDocumentTypeCode(DocumentTypeCode.SHI);
    shipmentEvent.setEventClassifierCode(EventClassifierCode.ACT);
    shipmentEvent.setDocumentID(shippingInstruction.getId());
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

    referenceTO = new ReferenceTO();
    referenceTO.setReferenceType(reference.getReferenceType());
    referenceTO.setReferenceValue(reference.getReferenceValue());

    consignmentItemTO =
        ConsignmentItemTO.builder()
            .descriptionOfGoods("Some description of the goods!")
            .hsCode("x".repeat(10))
            .volume(2.22)
            .weight(2.22)
            .cargoItems(List.of(cargoItemTO))
            .references(List.of(referenceTO))
            .build();

    utilizedTransportEquipmentTO = new UtilizedTransportEquipmentTO();
    utilizedTransportEquipmentTO.setEquipment(equipmentTO);
    utilizedTransportEquipmentTO.setSeals(List.of(sealsTO));
    utilizedTransportEquipmentTO.setCargoGrossWeight(120f);
    utilizedTransportEquipmentTO.setCargoGrossWeightUnit(WeightUnit.KGM);
    utilizedTransportEquipmentTO.setActiveReeferSettings(activeReeferSettingsTO);
    utilizedTransportEquipmentTO.setIsShipperOwned(true);

    documentPartyTO1 = new DocumentPartyTO();
    documentPartyTO1.setParty(partyMapper.partyToDTO(party));
    documentPartyTO1.setPartyFunction(PartyFunction.DDR);
    documentPartyTO1.setDisplayedAddress(List.of("displayedAddress"));

    documentPartyTO2 = new DocumentPartyTO();
    documentPartyTO2.setParty(partyMapper.partyToDTO(party));
    documentPartyTO2.setPartyFunction(PartyFunction.EBL);
    documentPartyTO2.setDisplayedAddress(List.of("displayedAddress"));

    shipmentTO = shipmentMapper.shipmentToDTO(shipment);
    shipmentTO.setTermsAndConditions("Fail Fast, Fail Early, Fail Often");

    shippingInstructionTO = shippingInstructionMapper.shippingInstructionToDTO(shippingInstruction);
    shippingInstructionTO.setCarrierBookingReference("XYZ12345");
    shippingInstructionTO.setPlaceOfIssue(locationTO);
    shippingInstructionTO.setConsignmentItems(List.of(consignmentItemTO));
    shippingInstructionTO.setUtilizedTransportEquipments(List.of(utilizedTransportEquipmentTO));
    shippingInstructionTO.setDocumentParties(List.of(documentPartyTO1, documentPartyTO2));
    shippingInstructionTO.setReferences(List.of(referenceTO));
    shippingInstructionTO.setShipments(List.of(shipmentTO));

    // Date & Time
    OffsetDateTime now = OffsetDateTime.now();
    shippingInstructionResponseTO =
        ShippingInstructionResponseTO.builder()
            .shippingInstructionUpdatedDateTime(now)
            .shippingInstructionCreatedDateTime(now)
            .build();
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
      when(shippingInstructionRepository.findById((UUID) any()))
          .thenReturn(Mono.just(shippingInstruction));
      when(locationService.createLocationByTO(any(), any())).thenReturn(Mono.just(locationTO));
      when(utilizedTransportEquipmentService.addUtilizedTransportEquipmentToShippingInstruction(
              any(), any()))
          .thenReturn(Mono.just(List.of(utilizedTransportEquipmentTO)));
      when(documentPartyService.createDocumentPartiesByShippingInstructionID(any(), any()))
          .thenReturn(Mono.just(List.of(documentPartyTO1, documentPartyTO2)));
      when(referenceService.createReferencesByShippingInstructionIdAndTOs(any(), any()))
          .thenReturn(Mono.just(List.of(referenceTO)));
      when(transportDocumentRepository.save(any())).thenReturn(Mono.empty());
      when(consignmentItemService.createConsignmentItemsByShippingInstructionIDAndTOs(
              any(), any(), any()))
          .thenReturn(
              Mono.just(
                  List.of(
                      consignmentItemTO.withCarrierBookingReference(
                          shippingInstructionTO.getCarrierBookingReference()))));
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
                verify(utilizedTransportEquipmentService)
                    .addUtilizedTransportEquipmentToShippingInstruction(any(), any());
                verify(documentPartyService)
                    .createDocumentPartiesByShippingInstructionID(any(), any());
                verify(referenceService)
                    .createReferencesByShippingInstructionIdAndTOs(any(), any());

                verify(shipmentEventService, times(2))
                    .create(argumentCaptorShipmentEvent.capture());
                assertEquals(
                    RECE,
                    argumentCaptorShipmentEvent.getAllValues().get(0).getShipmentEventTypeCode());
                assertEquals(
                    DRFT,
                    argumentCaptorShipmentEvent.getAllValues().get(1).getShipmentEventTypeCode());

                assertEquals(
                    shippingInstruction.getShippingInstructionReference(),
                    b.getShippingInstructionReference());
                assertEquals(DRFT, b.getDocumentStatus());
                assertNotNull(b.getShippingInstructionCreatedDateTime());
                assertNotNull(b.getShippingInstructionUpdatedDateTime());

                verify(transportDocumentRepository).save(any());
                verify(shippingInstructionMapper)
                    .dtoToShippingInstructionResponseTO(argumentCaptor.capture());
                assertEquals(
                    shippingInstruction.getShippingInstructionReference(),
                    argumentCaptor.getValue().getShippingInstructionReference());
                assertEquals(
                    shippingInstruction.getPlaceOfIssueID(),
                    argumentCaptor.getValue().getPlaceOfIssueID());
                assertEquals(
                    ShipmentEventTypeCode.DRFT, argumentCaptor.getValue().getDocumentStatus());
                assertNotNull(argumentCaptor.getValue().getPlaceOfIssue());
                assertNotNull(argumentCaptor.getValue().getDocumentParties());
                assertNotNull(argumentCaptor.getValue().getReferences());
                assertNotNull(argumentCaptor.getValue().getUtilizedTransportEquipments());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Method should save shipping instruction and return shipping response when carrierBookingReference is set on cargoItem")
    void testCreateShippingInstructionWithCarrierBookingReferenceOnCargoItem() {
      shippingInstructionTO.setCarrierBookingReference(null);
      shippingInstructionTO.setConsignmentItems(
          List.of(consignmentItemTO.withCarrierBookingReference("carrierBookingRequestReference")));
      utilizedTransportEquipmentTO.setCarrierBookingReference("carrierBookingRequestReference");

      when(bookingRepository.findAllByCarrierBookingReference(any()))
          .thenReturn(Flux.just(booking));
      when(shippingInstructionRepository.save(any())).thenReturn(Mono.just(shippingInstruction));
      when(shippingInstructionRepository.findById((UUID) any()))
          .thenReturn(Mono.just(shippingInstruction));
      when(locationService.createLocationByTO(any(), any())).thenReturn(Mono.just(locationTO));
      when(documentPartyService.createDocumentPartiesByShippingInstructionID(any(), any()))
          .thenReturn(Mono.just(List.of(documentPartyTO1, documentPartyTO2)));
      when(referenceService.createReferencesByShippingInstructionIdAndTOs(any(), any()))
          .thenReturn(Mono.just(List.of(referenceTO)));
      when(shipmentEventService.create(any()))
          .thenAnswer(arguments -> Mono.just(arguments.getArguments()[0]));
      when(transportDocumentRepository.save(any())).thenReturn(Mono.empty());
      when(consignmentItemService.createConsignmentItemsByShippingInstructionIDAndTOs(
              any(), any(), any()))
          .thenReturn(
              Mono.just(
                  List.of(
                      consignmentItemTO.withCarrierBookingReference(
                          "carrierBookingRequestReference"))));

      when(utilizedTransportEquipmentService.addUtilizedTransportEquipmentToShippingInstruction(
              any(), any()))
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
                verify(utilizedTransportEquipmentService)
                    .addUtilizedTransportEquipmentToShippingInstruction(any(), any());
                verify(documentPartyService)
                    .createDocumentPartiesByShippingInstructionID(any(), any());
                verify(referenceService)
                    .createReferencesByShippingInstructionIdAndTOs(any(), any());

                verify(shipmentEventService, times(2))
                    .create(argumentCaptorShipmentEvent.capture());
                assertEquals(
                    RECE,
                    argumentCaptorShipmentEvent.getAllValues().get(0).getShipmentEventTypeCode());
                assertEquals(
                    DRFT,
                    argumentCaptorShipmentEvent.getAllValues().get(1).getShipmentEventTypeCode());

                assertEquals(
                    shippingInstruction.getShippingInstructionReference(),
                    b.getShippingInstructionReference());
                assertEquals(DRFT, b.getDocumentStatus());
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
                    ShipmentEventTypeCode.DRFT, argumentCaptor.getValue().getDocumentStatus());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Method should save a shallow shipping instruction with isElectronic and no documentParties and return shipping response PENU")
    void testCreateShippingInstructionShallowIsElectronicTrueNoDocumentParties() {

      shippingInstructionTO.setIsElectronic(true);

      shippingInstructionTO.setPlaceOfIssue(null);
      shippingInstructionTO.setDocumentParties(null);
      shippingInstructionTO.setReferences(null);

      when(bookingRepository.findAllByCarrierBookingReference(any()))
          .thenReturn(Flux.just(booking));
      when(referenceService.createReferencesByShippingInstructionIdAndTOs(any(), any()))
          .thenReturn(Mono.empty());
      when(utilizedTransportEquipmentService.addUtilizedTransportEquipmentToShippingInstruction(
              any(), any()))
          .thenReturn(Mono.empty());
      when(shippingInstructionRepository.save(any())).thenReturn(Mono.just(shippingInstruction));
      when(shippingInstructionRepository.findById((UUID) any()))
          .thenReturn(Mono.just(shippingInstruction));
      when(consignmentItemService.createConsignmentItemsByShippingInstructionIDAndTOs(
              any(), any(), any()))
          .thenReturn(Mono.just(List.of(consignmentItemTO)));
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
                    shippingInstruction.getShippingInstructionReference(),
                    b.getShippingInstructionReference());
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

                verify(transportDocumentRepository, never()).save(any());
                verify(locationService, never()).createLocationByTO(any(), any());
                verify(documentPartyService, never())
                    .createDocumentPartiesByShippingInstructionID(any(), any());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Method should save a shipping instruction with isElectronic and EBL documentParties and return shipping response DRFT")
    void testCreateShippingInstructionShallowIsElectronicTrueOneEBLDocumentParty() {

      shippingInstructionTO.setIsElectronic(true);

      shippingInstructionTO.setPlaceOfIssue(null);
      shippingInstructionTO.setReferences(null);

      when(bookingRepository.findAllByCarrierBookingReference(any()))
          .thenReturn(Flux.just(booking));
      when(referenceService.createReferencesByShippingInstructionIdAndTOs(any(), any()))
          .thenReturn(Mono.empty());
      when(utilizedTransportEquipmentService.addUtilizedTransportEquipmentToShippingInstruction(
              any(), any()))
          .thenReturn(Mono.empty());
      when(shippingInstructionRepository.save(any())).thenReturn(Mono.just(shippingInstruction));
      when(shippingInstructionRepository.findById((UUID) any()))
          .thenReturn(Mono.just(shippingInstruction));
      when(transportDocumentRepository.save(any())).thenReturn(Mono.empty());
      when(consignmentItemService.createConsignmentItemsByShippingInstructionIDAndTOs(
              any(), any(), any()))
          .thenReturn(
              Mono.just(
                  List.of(
                      consignmentItemTO.withCarrierBookingReference(
                          shippingInstructionTO.getCarrierBookingReference()))));
      when(documentPartyService.createDocumentPartiesByShippingInstructionID(any(), any()))
          .thenReturn(Mono.just(List.of(documentPartyTO1, documentPartyTO2)));
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
                    shippingInstruction.getShippingInstructionReference(),
                    b.getShippingInstructionReference());
                assertEquals("Draft", b.getDocumentStatus().getValue());
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
                    "Draft",
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
                    ShipmentEventTypeCode.DRFT, argumentCaptor.getValue().getDocumentStatus());

                verify(locationService, never()).createLocationByTO(any(), any());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Method should save a shipping instruction with isElectronic and two EBL documentParties and return shipping response PENU")
    void testCreateShippingInstructionShallowIsElectronicTrueTwoEBLDocumentParty() {

      shippingInstructionTO.setIsElectronic(true);

      shippingInstructionTO.setPlaceOfIssue(null);
      shippingInstructionTO.setReferences(null);

      when(bookingRepository.findAllByCarrierBookingReference(any()))
          .thenReturn(Flux.just(booking));
      when(referenceService.createReferencesByShippingInstructionIdAndTOs(any(), any()))
          .thenReturn(Mono.empty());
      when(utilizedTransportEquipmentService.addUtilizedTransportEquipmentToShippingInstruction(
              any(), any()))
          .thenReturn(Mono.empty());
      when(shippingInstructionRepository.save(any())).thenReturn(Mono.just(shippingInstruction));
      when(shippingInstructionRepository.findById((UUID) any()))
          .thenReturn(Mono.just(shippingInstruction));
      when(consignmentItemService.createConsignmentItemsByShippingInstructionIDAndTOs(
              any(), any(), any()))
          .thenReturn(Mono.just(List.of(consignmentItemTO)));
      when(documentPartyService.createDocumentPartiesByShippingInstructionID(any(), any()))
          .thenReturn(Mono.just(List.of(documentPartyTO2, documentPartyTO2)));
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
                    shippingInstruction.getShippingInstructionReference(),
                    b.getShippingInstructionReference());
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
              })
          .verifyComplete();
    }

    @Test
    @DisplayName("Method should save a shallow shipping instruction and return shipping response")
    void testCreateShippingInstructionShallow() {

      shippingInstructionTO.setIsElectronic(false);
      shippingInstructionTO.setNumberOfOriginals(12);
      shippingInstructionTO.setNumberOfCopies(12);

      shippingInstructionTO.setPlaceOfIssue(null);
      shippingInstructionTO.setDocumentParties(null);
      shippingInstructionTO.setReferences(null);

      when(bookingRepository.findAllByCarrierBookingReference(any()))
          .thenReturn(Flux.just(booking));
      when(referenceService.createReferencesByShippingInstructionIdAndTOs(any(), any()))
          .thenReturn(Mono.empty());
      when(utilizedTransportEquipmentService.addUtilizedTransportEquipmentToShippingInstruction(
              any(), any()))
          .thenReturn(Mono.empty());
      when(shippingInstructionRepository.save(any())).thenReturn(Mono.just(shippingInstruction));
      when(shippingInstructionRepository.findById((UUID) any()))
          .thenReturn(Mono.just(shippingInstruction));
      when(transportDocumentRepository.save(any())).thenReturn(Mono.empty());
      when(consignmentItemService.createConsignmentItemsByShippingInstructionIDAndTOs(
              any(), any(), any()))
          .thenReturn(
              Mono.just(
                  List.of(
                      consignmentItemTO.withCarrierBookingReference(
                          shippingInstructionTO.getCarrierBookingReference()))));
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
                    shippingInstruction.getShippingInstructionReference(),
                    b.getShippingInstructionReference());
                assertEquals(DRFT, b.getDocumentStatus());
                assertNotNull(b.getShippingInstructionCreatedDateTime());
                assertNotNull(b.getShippingInstructionUpdatedDateTime());

                verify(shipmentEventService, times(2))
                    .create(argumentCaptorShipmentEvent.capture());
                assertEquals(
                    RECE,
                    argumentCaptorShipmentEvent.getAllValues().get(0).getShipmentEventTypeCode());
                assertEquals(
                    DRFT,
                    argumentCaptorShipmentEvent.getAllValues().get(1).getShipmentEventTypeCode());

                verify(shippingInstructionMapper)
                    .dtoToShippingInstructionResponseTO(argumentCaptor.capture());
                assertEquals(
                    shippingInstruction.getShippingInstructionReference(),
                    argumentCaptor.getValue().getShippingInstructionReference());
                assertEquals(
                    shippingInstruction.getPlaceOfIssueID(),
                    argumentCaptor.getValue().getPlaceOfIssueID());
                assertEquals(
                    ShipmentEventTypeCode.DRFT, argumentCaptor.getValue().getDocumentStatus());

                verify(locationService, never()).createLocationByTO(any(), any());
                verify(transportDocumentRepository).save(any());
                verify(documentPartyService, never())
                    .createDocumentPartiesByShippingInstructionID(any(), any());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName("Method should save a shallow shipping with validation errors resulting in PENU")
    void testCreateShippingInstructionResultingInPENU() {

      shippingInstructionTO.setPlaceOfIssue(null);
      shippingInstructionTO.setDocumentParties(null);
      shippingInstructionTO.setReferences(null);
      shippingInstructionTO.setIsElectronic(false);
      shippingInstructionTO.setNumberOfCopies(null);

      when(bookingRepository.findAllByCarrierBookingReference(any()))
          .thenReturn(Flux.just(booking));
      when(referenceService.createReferencesByShippingInstructionIdAndTOs(any(), any()))
          .thenReturn(Mono.empty());
      when(utilizedTransportEquipmentService.addUtilizedTransportEquipmentToShippingInstruction(
              any(), any()))
          .thenReturn(Mono.empty());
      when(consignmentItemService.createConsignmentItemsByShippingInstructionIDAndTOs(
              any(), any(), any()))
          .thenReturn(Mono.just(List.of(consignmentItemTO)));
      when(shippingInstructionRepository.save(any())).thenReturn(Mono.just(shippingInstruction));
      when(shippingInstructionRepository.findById((UUID) any()))
          .thenReturn(Mono.just(shippingInstruction));
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
                    shippingInstruction.getShippingInstructionReference(),
                    b.getShippingInstructionReference());
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
                    .createDocumentPartiesByShippingInstructionID(any(), any());
                assertNull(argumentCaptor.getValue().getPlaceOfIssue());
                assertNull(argumentCaptor.getValue().getDocumentParties());
                assertNull(argumentCaptor.getValue().getReferences());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName("Failing to create a shipment event should result in error")
    void testShipmentEventFailedShouldResultInError() {

      shippingInstructionTO.setPlaceOfIssue(null);
      shippingInstructionTO.setDocumentParties(null);
      shippingInstructionTO.setReferences(null);

      when(bookingRepository.findAllByCarrierBookingReference(any()))
          .thenReturn(Flux.just(booking));
      when(shippingInstructionRepository.save(any()))
          .thenAnswer(arguments -> Mono.just(shippingInstruction));
      when(shippingInstructionRepository.findById((UUID) any()))
          .thenReturn(Mono.just(shippingInstruction));
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
        "Fail if ShippingInstruction contains no carrierBookingReference on SI and UtilizedTransportEquipment is null")
    void testCreateBookingShouldFailWithNoCarrierBookingReferenceAndNoUtilizedTransportEquipment() {

      shippingInstructionTO.setCarrierBookingReference(null);
      shippingInstructionTO.setUtilizedTransportEquipments(null);

      StepVerifier.create(
              shippingInstructionServiceImpl.createShippingInstruction(shippingInstructionTO))
          .expectErrorSatisfies(
              throwable -> {
                Assertions.assertTrue(throwable instanceof ConcreteRequestErrorMessageException);
                assertEquals(
                    "CarrierBookingReference needs to be defined on either ShippingInstruction, UtilizedTransportEquipment or ConsignmentItem level.",
                    throwable.getMessage());
              })
          .verify();
    }

    @Test
    @DisplayName(
        "Fail if ShippingInstruction does not contain any carrierBookingReference on any level")
    void testCreateBookingShouldFailWithNoCarrierBookingReference() {

      shippingInstructionTO.setCarrierBookingReference(null);
      utilizedTransportEquipmentTO.setCargoItems(List.of(cargoItemTO));
      utilizedTransportEquipmentTO.setCarrierBookingReference(null);
      shippingInstructionTO.setUtilizedTransportEquipments(List.of(utilizedTransportEquipmentTO));

      StepVerifier.create(
              shippingInstructionServiceImpl.createShippingInstruction(shippingInstructionTO))
          .expectErrorSatisfies(
              throwable -> {
                Assertions.assertTrue(throwable instanceof ConcreteRequestErrorMessageException);
                assertEquals(
                    "CarrierBookingReference needs to be defined on either ShippingInstruction, UtilizedTransportEquipment or ConsignmentItem level.",
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
                          + utilizedTransportEquipmentTO.getCarrierBookingReference()
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
                    "No booking found for carrier booking reference: "
                        + utilizedTransportEquipmentTO.getCarrierBookingReference(),
                    throwable.getMessage());
              })
          .verify();
    }

    @Test
    @DisplayName(
        "Fail if ShippingInstruction contains carrierBookingReference on both root and in UtilizedTransportEquipments")
    void
        testCreateShippingInstructionShouldFailWithCarrierBookingReferenceInRootAndInUtilizedTransportEquipment() {

      utilizedTransportEquipmentTO.setCarrierBookingReference("CarrierBookingReference");

      StepVerifier.create(
              shippingInstructionServiceImpl.createShippingInstruction(shippingInstructionTO))
          .expectErrorSatisfies(
              throwable -> {
                Assertions.assertTrue(throwable instanceof ConcreteRequestErrorMessageException);
                assertEquals(
                    "CarrierBookingReference defined on both ShippingInstruction and UtilizedTransportEquipment level.",
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
      when(transportDocumentRepository.save(any())).thenReturn(Mono.empty());
      when(consignmentItemService.createConsignmentItemsByShippingInstructionIDAndTOs(
              any(), any(), any()))
          .thenReturn(
              Mono.just(
                  List.of(
                      consignmentItemTO.withCarrierBookingReference(
                          shippingInstructionTO.getCarrierBookingReference()))));

      // finds
      when(bookingRepository.findAllByCarrierBookingReference(any()))
          .thenReturn(Flux.just(booking));
      when(shippingInstructionRepository.findByShippingInstructionReference(any(String.class)))
          .thenReturn(Mono.just(shippingInstruction));

      // deletes
      when(utilizedTransportEquipmentService
              .resolveUtilizedTransportEquipmentsForShippingInstructionReference(any(), any()))
          .thenReturn(Mono.empty());
      when(documentPartyService.resolveDocumentPartiesForShippingInstructionID(any(), any()))
          .thenReturn(Mono.empty());
      when(referenceService.resolveReferencesForShippingInstructionReference(any(), any()))
          .thenReturn(Mono.empty());

      ArgumentCaptor<ShippingInstructionTO> argumentCaptor =
          ArgumentCaptor.forClass(ShippingInstructionTO.class);

      ArgumentCaptor<ShipmentEvent> argumentCaptorShipmentEvent =
          ArgumentCaptor.forClass(ShipmentEvent.class);

      StepVerifier.create(
              shippingInstructionServiceImpl
                  .updateShippingInstructionByShippingInstructionReference(
                      shippingInstruction.getShippingInstructionReference(), shippingInstructionTO))
          .assertNext(
              b -> {
                verify(locationService).resolveLocationByTO(any(), any(), any());
                verify(utilizedTransportEquipmentService)
                    .resolveUtilizedTransportEquipmentsForShippingInstructionReference(
                        any(), any());
                verify(documentPartyService)
                    .resolveDocumentPartiesForShippingInstructionID(any(), any());
                verify(referenceService)
                    .resolveReferencesForShippingInstructionReference(any(), any());

                verify(shipmentEventService, times(2))
                    .create(argumentCaptorShipmentEvent.capture());
                assertEquals(
                    ShipmentEventTypeCode.PENU,
                    argumentCaptorShipmentEvent.getAllValues().get(0).getShipmentEventTypeCode());
                assertEquals(
                    ShipmentEventTypeCode.DRFT,
                    argumentCaptorShipmentEvent.getAllValues().get(1).getShipmentEventTypeCode());

                assertEquals(
                    shippingInstruction.getShippingInstructionReference(),
                    b.getShippingInstructionReference());
                assertEquals(DRFT, b.getDocumentStatus());
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
                    ShipmentEventTypeCode.DRFT, argumentCaptor.getValue().getDocumentStatus());
                assertNotNull(argumentCaptor.getValue().getPlaceOfIssue());
                assertNotNull(argumentCaptor.getValue().getDocumentParties());
                assertNotNull(argumentCaptor.getValue().getReferences());
                assertNotNull(argumentCaptor.getValue().getUtilizedTransportEquipments());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Method should update an existing shallow shipping instruction and return shipping response")
    void testUpdateShippingInstructionShallow() {

      shippingInstructionTO.setIsElectronic(false);
      shippingInstructionTO.setNumberOfOriginals(12);
      shippingInstructionTO.setNumberOfCopies(12);

      shippingInstructionTO.setPlaceOfIssue(null);
      shippingInstructionTO.setDocumentParties(null);
      shippingInstructionTO.setReferences(null);
      shippingInstructionTO.setUtilizedTransportEquipments(null);

      shippingInstruction.setDocumentStatus(ShipmentEventTypeCode.PENU);

      // saves
      when(shippingInstructionRepository.save(any())).thenReturn(Mono.just(shippingInstruction));
      when(locationService.resolveLocationByTO(any(), any(), any()))
          .thenReturn(Mono.just(locationTO));
      when(shipmentEventService.create(any()))
          .thenAnswer(arguments -> Mono.just(arguments.getArguments()[0]));
      when(transportDocumentRepository.save(any())).thenReturn(Mono.empty());
      when(consignmentItemService.createConsignmentItemsByShippingInstructionIDAndTOs(
              any(), any(), any()))
          .thenReturn(
              Mono.just(
                  List.of(
                      consignmentItemTO.withCarrierBookingReference(
                          shippingInstructionTO.getCarrierBookingReference()))));

      // finds
      when(bookingRepository.findAllByCarrierBookingReference(any()))
          .thenReturn(Flux.just(booking));
      when(shippingInstructionRepository.findByShippingInstructionReference(any(String.class)))
          .thenReturn(Mono.just(shippingInstruction));

      // deletes
      when(utilizedTransportEquipmentService
              .resolveUtilizedTransportEquipmentsForShippingInstructionReference(any(), any()))
          .thenReturn(Mono.empty());
      when(documentPartyService.resolveDocumentPartiesForShippingInstructionID(any(), any()))
          .thenReturn(Mono.empty());
      when(referenceService.resolveReferencesForShippingInstructionReference(any(), any()))
          .thenReturn(Mono.empty());

      ArgumentCaptor<ShippingInstructionTO> argumentCaptor =
          ArgumentCaptor.forClass(ShippingInstructionTO.class);

      ArgumentCaptor<ShipmentEvent> argumentCaptorShipmentEvent =
          ArgumentCaptor.forClass(ShipmentEvent.class);

      StepVerifier.create(
              shippingInstructionServiceImpl
                  .updateShippingInstructionByShippingInstructionReference(
                      shippingInstruction.getShippingInstructionReference(), shippingInstructionTO))
          .assertNext(
              b -> {
                assertEquals(
                    shippingInstruction.getShippingInstructionReference(),
                    b.getShippingInstructionReference());
                assertEquals(DRFT, b.getDocumentStatus());
                assertNotNull(b.getShippingInstructionCreatedDateTime());
                assertNotNull(b.getShippingInstructionUpdatedDateTime());

                verify(shipmentEventService, times(2))
                    .create(argumentCaptorShipmentEvent.capture());
                assertEquals(
                    ShipmentEventTypeCode.PENU,
                    argumentCaptorShipmentEvent.getAllValues().get(0).getShipmentEventTypeCode());
                assertEquals(
                    ShipmentEventTypeCode.DRFT,
                    argumentCaptorShipmentEvent.getAllValues().get(1).getShipmentEventTypeCode());

                verify(shippingInstructionMapper)
                    .dtoToShippingInstructionResponseTO(argumentCaptor.capture());
                assertEquals(
                    shippingInstruction.getShippingInstructionReference(),
                    argumentCaptor.getValue().getShippingInstructionReference());
                assertEquals(
                    shippingInstruction.getPlaceOfIssueID(),
                    argumentCaptor.getValue().getPlaceOfIssueID());
                assertEquals(
                    ShipmentEventTypeCode.DRFT, argumentCaptor.getValue().getDocumentStatus());

                verify(locationService, never()).createLocationByTO(any(), any());
                verify(shipmentRepository, never()).findByCarrierBookingReference(any());
                verify(utilizedTransportEquipmentService, never())
                    .createUtilizedTransportEquipment(any(), any(), any());
                verify(documentPartyService, never())
                    .createDocumentPartiesByShippingInstructionID(any(), any());
                verify(referenceService, never())
                    .createReferencesByShippingInstructionIdAndTOs(any(), any());
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
      shippingInstructionTO.setUtilizedTransportEquipments(null);
      shippingInstructionTO.setIsElectronic(false);
      shippingInstructionTO.setNumberOfCopies(null);

      // saves
      when(shippingInstructionRepository.save(any())).thenReturn(Mono.just(shippingInstruction));
      when(locationService.resolveLocationByTO(any(), any(), any())).thenReturn(Mono.empty());
      when(consignmentItemService.createConsignmentItemsByShippingInstructionIDAndTOs(
              any(), any(), any()))
          .thenReturn(Mono.just(List.of(consignmentItemTO)));
      when(shipmentEventService.create(any()))
          .thenAnswer(arguments -> Mono.just(arguments.getArguments()[0]));

      // finds
      when(bookingRepository.findAllByCarrierBookingReference(any()))
          .thenReturn(Flux.just(booking));
      when(shippingInstructionRepository.findByShippingInstructionReference(any(String.class)))
          .thenReturn(Mono.just(shippingInstruction));

      // deletes
      when(utilizedTransportEquipmentService
              .resolveUtilizedTransportEquipmentsForShippingInstructionReference(any(), any()))
          .thenReturn(Mono.empty());
      when(documentPartyService.resolveDocumentPartiesForShippingInstructionID(any(), any()))
          .thenReturn(Mono.empty());
      when(referenceService.resolveReferencesForShippingInstructionReference(any(), any()))
          .thenReturn(Mono.empty());

      ArgumentCaptor<ShippingInstructionTO> argumentCaptor =
          ArgumentCaptor.forClass(ShippingInstructionTO.class);

      ArgumentCaptor<ShipmentEvent> argumentCaptorShipmentEvent =
          ArgumentCaptor.forClass(ShipmentEvent.class);

      StepVerifier.create(
              shippingInstructionServiceImpl
                  .updateShippingInstructionByShippingInstructionReference(
                      shippingInstruction.getShippingInstructionReference(), shippingInstructionTO))
          .assertNext(
              b -> {
                assertEquals(
                    shippingInstruction.getShippingInstructionReference(),
                    b.getShippingInstructionReference());
                assertEquals(PENU, b.getDocumentStatus());
                assertNotNull(b.getShippingInstructionCreatedDateTime());
                assertNotNull(b.getShippingInstructionUpdatedDateTime());

                verify(shipmentEventService, times(2))
                    .create(argumentCaptorShipmentEvent.capture());
                assertEquals(
                    ShipmentEventTypeCode.PENU,
                    argumentCaptorShipmentEvent.getAllValues().get(0).getShipmentEventTypeCode());
                assertEquals(
                    PENU,
                    argumentCaptorShipmentEvent.getAllValues().get(1).getShipmentEventTypeCode());

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

                verify(transportDocumentRepository, never()).save(any());
                verify(locationService, never()).createLocationByTO(any(), any());
                verify(shipmentRepository, never()).findByCarrierBookingReference(any());
                verify(utilizedTransportEquipmentService, never())
                    .createUtilizedTransportEquipment(any(), any(), any());
                verify(documentPartyService, never())
                    .createDocumentPartiesByShippingInstructionID(any(), any());
                verify(referenceService, never())
                    .createReferencesByShippingInstructionIdAndTOs(any(), any());
                assertNull(argumentCaptor.getValue().getPlaceOfIssue());
                assertNull(argumentCaptor.getValue().getDocumentParties());
                assertNull(argumentCaptor.getValue().getReferences());
                assertNull(argumentCaptor.getValue().getUtilizedTransportEquipments());
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
      shippingInstructionTO.setUtilizedTransportEquipments(null);

      when(shipmentEventService.create(any())).thenAnswer(arguments -> Mono.empty());
      when(bookingRepository.findAllByCarrierBookingReference(any()))
          .thenReturn(Flux.just(booking));
      when(shippingInstructionRepository.findByShippingInstructionReference(any(String.class)))
          .thenReturn(Mono.just(shippingInstruction));

      StepVerifier.create(
              shippingInstructionServiceImpl
                  .updateShippingInstructionByShippingInstructionReference(
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
    void testUpdateBookingShouldFailWithNoCarrierBookingReferenceAndNoUtilizedTransportEquipment() {

      shippingInstructionTO.setUtilizedTransportEquipments(null);

      when(shippingInstructionRepository.findByShippingInstructionReference(any(String.class)))
          .thenReturn(Mono.empty());
      when(bookingRepository.findAllByCarrierBookingReference(any()))
          .thenReturn(Flux.just(booking));

      StepVerifier.create(
              shippingInstructionServiceImpl
                  .updateShippingInstructionByShippingInstructionReference(
                      shippingInstruction.getShippingInstructionReference(), shippingInstructionTO))
          .expectErrorSatisfies(
              throwable -> {
                Assertions.assertTrue(throwable instanceof ConcreteRequestErrorMessageException);
                assertEquals(
                    "No Shipping Instruction found with reference: "
                        + shippingInstruction.getShippingInstructionReference(),
                    throwable.getMessage());
              })
          .verify();
    }

    @Test
    @DisplayName(
        "Fail if ShippingInstruction contains carrierBookingReference on both root and in CargoItems")
    void testUpdateBookingShouldFailWithCarrierBookingReferenceInRootAndInCargoItem() {

      utilizedTransportEquipmentTO.setCarrierBookingReference("CarrierBookingReference");

      StepVerifier.create(
              shippingInstructionServiceImpl
                  .updateShippingInstructionByShippingInstructionReference(
                      shippingInstruction.getShippingInstructionReference(), shippingInstructionTO))
          .expectErrorSatisfies(
              throwable -> {
                Assertions.assertTrue(throwable instanceof ConcreteRequestErrorMessageException);
                assertEquals(
                    "CarrierBookingReference defined on both ShippingInstruction and UtilizedTransportEquipment level.",
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
      utilizedTransportEquipmentTO.setCarrierBookingReference(null);
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
      utilizedTransportEquipmentTO.setCarrierBookingReference("CarrierBookingReference");
      shippingInstructionTO.setConsignmentItems(
          List.of(consignmentItemTO.withCarrierBookingReference("CarrierBookingReference")));
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
      utilizedTransportEquipmentTO.setIsShipperOwned(true);
      utilizedTransportEquipmentTO.getEquipment().setTareWeight(null);
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
      utilizedTransportEquipmentTO.setIsShipperOwned(true);
      utilizedTransportEquipmentTO.getEquipment().setTareWeight(null);
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

      when(shippingInstructionRepository.findByShippingInstructionReference(any(String.class)))
          .thenReturn(Mono.empty());

      StepVerifier.create(
              shippingInstructionServiceImpl.findByReference(invalidShippingInstructionReference))
          .expectErrorSatisfies(
              throwable -> {
                Assertions.assertTrue(throwable instanceof ConcreteRequestErrorMessageException);
                assertEquals(
                    "No Shipping Instruction found with reference: "
                        + invalidShippingInstructionReference,
                    throwable.getMessage());
              })
          .verify();
    }

    @Test
    @DisplayName("Test GET shipping instruction for an assumed valid ID.")
    void testGetShippingInstructionForValidID() {
      String stubbedCRef = UUID.randomUUID().toString();
      when(shippingInstructionRepository.findByShippingInstructionReference(any(String.class)))
          .thenReturn(Mono.just(shippingInstruction));
      when(shippingInstructionRepository.findCarrierBookingReferenceByShippingInstructionID(any()))
          .thenReturn(Flux.just(stubbedCRef));
      when(locationService.fetchLocationByID(any())).thenReturn(Mono.just(locationTO));
      UUID sID1 = UUID.randomUUID();
      UUID sID2 = UUID.randomUUID();
      when(shippingInstructionRepository.findShipmentIDsByShippingInstructionID(any()))
          .thenReturn(Flux.just(sID1, sID2));
      when(utilizedTransportEquipmentService.findUtilizedTransportEquipmentByShipmentID(sID1))
          .thenReturn(Mono.just(Collections.singletonList(utilizedTransportEquipmentTO)));
      when(utilizedTransportEquipmentService.findUtilizedTransportEquipmentByShipmentID(sID2))
          .thenReturn(Mono.just(Collections.singletonList(utilizedTransportEquipmentTO)));
      when(documentPartyService.fetchDocumentPartiesByByShippingInstructionID(any()))
          .thenReturn(Mono.just(Collections.singletonList(documentPartyTO1)));
      when(referenceService.findByShippingInstructionID(any()))
          .thenReturn(Mono.just(Collections.singletonList(referenceTO)));
      when(shipmentService.findByShippingInstructionReference(any()))
          .thenReturn(Mono.just(Collections.singletonList(shipmentTO)));
      when(consignmentItemService.fetchConsignmentItemsTOByShippingInstructionID(any()))
          .thenReturn(Mono.just(Collections.singletonList(consignmentItemTO)));

      StepVerifier.create(
              shippingInstructionServiceImpl.findByReference(UUID.randomUUID().toString()))
          .assertNext(
              result -> {
                assertEquals(stubbedCRef, result.getCarrierBookingReference());
                assertEquals("Hamburg", result.getPlaceOfIssue().getLocationName());
                assertEquals(2, result.getUtilizedTransportEquipments().size());
                assertEquals(
                    "APZU4812090",
                    result
                        .getUtilizedTransportEquipments()
                        .get(0)
                        .getEquipment()
                        .getEquipmentReference());
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
                          + utilizedTransportEquipmentTO.getCarrierBookingReference()
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
                    "No booking found for carrier booking reference: "
                        + utilizedTransportEquipmentTO.getCarrierBookingReference(),
                    throwable.getMessage());
              })
          .verify();
    }
  }
}
