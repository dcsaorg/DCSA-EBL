package org.dcsa.ebl.service.impl;

import org.dcsa.core.events.edocumentation.model.transferobject.BookingTO;
import org.dcsa.core.events.edocumentation.model.transferobject.CarrierClauseTO;
import org.dcsa.core.events.edocumentation.model.transferobject.ChargeTO;
import org.dcsa.core.events.edocumentation.model.transferobject.ShipmentTO;
import org.dcsa.core.events.edocumentation.service.CarrierClauseService;
import org.dcsa.core.events.edocumentation.service.ChargeService;
import org.dcsa.core.events.edocumentation.service.ShipmentService;
import org.dcsa.core.events.model.*;
import org.dcsa.core.events.model.enums.CarrierCodeListProvider;
import org.dcsa.core.events.model.enums.PaymentTerm;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.core.events.model.enums.WeightUnit;
import org.dcsa.core.events.model.transferobjects.LocationTO;
import org.dcsa.core.events.model.transferobjects.ShippingInstructionTO;
import org.dcsa.core.events.repository.BookingRepository;
import org.dcsa.core.events.repository.CarrierRepository;
import org.dcsa.core.events.repository.TransportDocumentRepository;
import org.dcsa.core.events.service.LocationService;
import org.dcsa.core.events.service.ShipmentEventService;
import org.dcsa.core.exception.ConcreteRequestErrorMessageException;
import org.dcsa.ebl.model.mappers.TransportDocumentMapper;
import org.dcsa.ebl.model.transferobjects.TransportDocumentTO;
import org.dcsa.ebl.repository.ShippingInstructionRepository;
import org.dcsa.ebl.service.ShippingInstructionService;
import org.dcsa.ebl.service.TransportDocumentService;
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

  @Mock ShippingInstructionService shippingInstructionService;
  @Mock ChargeService chargeService;
  @Mock CarrierClauseService carrierClauseService;
  @Mock LocationService locationService;
  @Mock ShipmentService shipmentService;
  @Mock ShipmentEventService shipmentEventService;
  @Mock TransportDocumentService transportDocumentService;

  @InjectMocks TransportDocumentServiceImpl transportDocumentServiceImpl;

  @Spy
  TransportDocumentMapper transportDocumentMapper =
      Mappers.getMapper(TransportDocumentMapper.class);

  CargoItem cargoItem;
  Shipment shipment;
  ShipmentEquipment shipmentEquipment;
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

  @BeforeEach
  void init() {
    OffsetDateTime now = OffsetDateTime.now();
    carrier = new Carrier();
    carrier.setId(UUID.randomUUID());
    carrier.setNmftaCode("x".repeat(4));
    carrier.setSmdgCode(null);
    carrier.setCarrierName("Klods-Hans");

    shippingInstruction = new ShippingInstruction();
    shippingInstruction.setShippingInstructionReference(UUID.randomUUID().toString());
    shippingInstruction.setDocumentStatus(ShipmentEventTypeCode.RECE);
    shippingInstruction.setShippingInstructionCreatedDateTime(now);
    shippingInstruction.setShippingInstructionUpdatedDateTime(now);

    shipment = new Shipment();
    shipment.setShipmentID(UUID.randomUUID());
    shipment.setCarrierBookingReference("x".repeat(35));

    shipmentEquipment = new ShipmentEquipment();
    shipmentEquipment.setId(UUID.randomUUID());
    shipmentEquipment.setShipmentID(shipment.getShipmentID());
    shipmentEquipment.setIsShipperOwned(false);
    shipmentEquipment.setCargoGrossWeightUnit(WeightUnit.KGM);
    shipmentEquipment.setCargoGrossWeight(21f);
    shipmentEquipment.setShipmentID(shipment.getShipmentID());

    cargoItem = new CargoItem();
    cargoItem.setId(UUID.randomUUID());
    cargoItem.setHsCode("x".repeat(10));
    cargoItem.setDescriptionOfGoods("Some description of the goods!");
    cargoItem.setNumberOfPackages(2);
    cargoItem.setPackageCode("XYZ");
    cargoItem.setShipmentEquipmentID(shipmentEquipment.getId());
    cargoItem.setShippingInstructionReference(shippingInstruction.getShippingInstructionReference());

    transportDocument = new TransportDocument();
    transportDocument.setShippingInstructionReference(shippingInstruction.getShippingInstructionReference());
    transportDocument.setTransportDocumentReference("TransportDocumentReference1");
    transportDocument.setIssuer(carrier.getId());
    transportDocument.setIssueDate(LocalDate.now());
    transportDocument.setTransportDocumentRequestCreatedDateTime(now);
    transportDocument.setTransportDocumentRequestUpdatedDateTime(now);
    transportDocument.setDeclaredValue(12f);
    transportDocument.setDeclaredValueCurrency("DKK");
    transportDocument.setReceivedForShipmentDate(LocalDate.now());
    transportDocument.setPlaceOfIssue("1");

    Address address = new Address();
    address.setCity("Amsterdam");
    address.setCountry("Netherlands");
    address.setStreet("Strawinskylaan");
    address.setPostalCode("1077ZX");
    address.setStreetNumber("4117");
    address.setFloor("6");
    address.setStateRegion("Noord-Holland");

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
    shippingInstructionTO.setDocumentStatus(ShipmentEventTypeCode.PENA);
    shippingInstructionTO.setPlaceOfIssueID(locationTO.getId());
    shippingInstructionTO.setAreChargesDisplayedOnCopies(true);
    shippingInstructionTO.setShippingInstructionUpdatedDateTime(OffsetDateTime.now());
    shippingInstructionTO.setShipments(List.of(shipmentTO));

    transportDocumentTO = new TransportDocumentTO();
    transportDocumentTO.setCharges(List.of(chargeTO));
    transportDocumentTO.setPlaceOfIssue(locationTO);
    transportDocumentTO.setCarrierClauses(List.of(carrierClauseTO));
    transportDocumentTO.setShippingInstruction(shippingInstructionTO);
    transportDocumentTO.setTransportDocumentReference("TransportDocumentReference1");

    booking = new Booking();
    booking.setId(UUID.randomUUID());
    booking.setDocumentStatus(ShipmentEventTypeCode.CONF);
    booking.setCarrierBookingRequestReference(UUID.randomUUID().toString());
  }

  @Nested
  @DisplayName("Tests for the method findAll(#DocumentStatus,#CarrierBookingReference)")
  class GetTransportDocumentSummaryTest {

    @Test
    @DisplayName("Test GET shipping instruction with everything for a valid ID.")
    void testGetTransportDocumentWithEverythingForValidID() {
      when(carrierRepository.findById(any(UUID.class))).thenReturn(Mono.just(carrier));
      when(shippingInstructionRepository.findById(any(String.class)))
          .thenReturn(Mono.just(shippingInstruction));
        when(shippingInstructionRepository.findCarrierBookingReferenceByShippingInstructionReference(any()))
          .thenReturn(Flux.just(shipment.getCarrierBookingReference()));
      StepVerifier.create(transportDocumentServiceImpl.mapDM2TO(transportDocument))
          .assertNext(
            result -> {
              assertNotNull(result.getTransportDocumentReference());
              assertNotNull(result.getIssuerCode());
              assertNotNull(result.getIssueDate());
              assertNotNull(result.getTransportDocumentRequestCreatedDateTime());
              assertNotNull(result.getTransportDocumentRequestUpdatedDateTime());
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
      when(shippingInstructionRepository.findById(any(String.class)))
          .thenReturn(Mono.just(shippingInstruction));
        when(shippingInstructionRepository.findCarrierBookingReferenceByShippingInstructionReference(any()))
          .thenReturn(Flux.empty());

      StepVerifier.create(transportDocumentServiceImpl.mapDM2TO(transportDocument))
          .assertNext(
            result -> {
              assertNotNull(result.getTransportDocumentReference());
              assertNotNull(result.getIssuerCode());
              assertNotNull(result.getIssueDate());
              assertNotNull(result.getTransportDocumentRequestCreatedDateTime());
              assertNotNull(result.getTransportDocumentRequestUpdatedDateTime());
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

      when(shippingInstructionRepository.findById(any(String.class)))
          .thenReturn(Mono.just(shippingInstruction));
        when(shippingInstructionRepository.findCarrierBookingReferenceByShippingInstructionReference(any()))
          .thenReturn(Flux.just(shipment.getCarrierBookingReference()));

      StepVerifier.create(transportDocumentServiceImpl.mapDM2TO(transportDocument))
          .assertNext(
              result -> {
                verify(carrierRepository, never()).findById(any(UUID.class));

                assertNotNull(result.getTransportDocumentRequestCreatedDateTime());
                assertNotNull(result.getTransportDocumentRequestUpdatedDateTime());
                assertEquals(shippingInstruction.getDocumentStatus(), result.getDocumentStatus());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName("Test GET transport document summaries for invalid issuer")
    void testGetTransportDocumentWithInvalidIssuer() {

      when(shippingInstructionRepository.findById(any(String.class)))
          .thenReturn(Mono.just(shippingInstruction));
      when(shippingInstructionRepository.findCarrierBookingReferenceByShippingInstructionReference(any()))
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

      when(shippingInstructionRepository.findById(any(String.class))).thenReturn(Mono.empty());

      StepVerifier.create(transportDocumentServiceImpl.mapDM2TO(transportDocument))
          .expectErrorSatisfies(
              throwable -> {
                Assertions.assertTrue(throwable instanceof ConcreteRequestErrorMessageException);
                assertEquals(
                    "No shipping instruction was found with reference: "
                        + transportDocument.getShippingInstructionReference(),
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

      when(transportDocumentRepository.findById((String) any()))
          .thenReturn(Mono.just(transportDocument));
      when(carrierRepository.findById((UUID) any())).thenReturn(Mono.just(carrier));
      when(locationService.fetchLocationDeepObjByID(shippingInstructionTO.getPlaceOfIssueID()))
          .thenReturn(Mono.just(locationTO));
        when(shippingInstructionService.findById(transportDocument.getShippingInstructionReference()))
          .thenReturn(Mono.just(shippingInstructionTO));
      when(chargeService.fetchChargesByTransportDocumentReference(
              transportDocumentTO.getTransportDocumentReference()))
          .thenReturn(Flux.just(chargeTO));
      when(carrierClauseService.fetchCarrierClausesByTransportDocumentReference(
              transportDocumentTO.getTransportDocumentReference()))
          .thenReturn(Flux.just(carrierClauseTO));

      StepVerifier.create(
              transportDocumentServiceImpl.findByTransportDocumentReference(
                  "TransportDocumentReference1"))
          .assertNext(
              transportDocumentTOResponse -> {
                assertEquals(carrier.getSmdgCode(), transportDocumentTOResponse.getIssuerCode());
                assertEquals(1, transportDocumentTOResponse.getCharges().size());
                assertNotNull(transportDocumentTOResponse.getPlaceOfIssue());
                assertEquals(1, transportDocumentTOResponse.getCarrierClauses().size());
                assertEquals(
                    1, transportDocumentTOResponse.getShippingInstruction().getShipments().size());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Get transport document without place of issue should return transport document without place of issue")
    void testFindTransportDocumentWithoutPlaceOfIssue() {

      carrier.setNmftaCode(null);

      transportDocument.setPlaceOfIssue(null);
      when(transportDocumentRepository.findById((String) any()))
          .thenReturn(Mono.just(transportDocument));
        when(carrierRepository.findById((UUID) any())).thenReturn(Mono.just(carrier));
        when(locationService.fetchLocationDeepObjByID(any())).thenReturn(Mono.empty());
        when(shippingInstructionService.findById(transportDocument.getShippingInstructionReference()))
          .thenReturn(Mono.just(shippingInstructionTO));
      when(chargeService.fetchChargesByTransportDocumentReference(
              transportDocumentTO.getTransportDocumentReference()))
          .thenReturn(Flux.just(chargeTO));
      when(carrierClauseService.fetchCarrierClausesByTransportDocumentReference(
              transportDocumentTO.getTransportDocumentReference()))
          .thenReturn(Flux.just(carrierClauseTO));

      StepVerifier.create(
              transportDocumentServiceImpl.findByTransportDocumentReference(
                  "TransportDocumentReference1"))
          .assertNext(
              transportDocumentTOResponse -> {
                assertEquals(carrier.getSmdgCode(), transportDocumentTOResponse.getIssuerCode());
                assertEquals(1, transportDocumentTOResponse.getCharges().size());
                assertNull(transportDocumentTOResponse.getPlaceOfIssue());
                assertEquals(1, transportDocumentTOResponse.getCarrierClauses().size());
                assertEquals(
                    1, transportDocumentTOResponse.getShippingInstruction().getShipments().size());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Get transport document without top level carrierBookingReferences should return transport document without carrierBookingReferences on top level.")
    void testFindTransportDocumentWithoutCarrierBookingReferences() {

      carrier.setNmftaCode(null);

      when(transportDocumentRepository.findById((String) any()))
          .thenReturn(Mono.just(transportDocument));
      when(carrierRepository.findById((UUID) any())).thenReturn(Mono.just(carrier));
      when(locationService.fetchLocationDeepObjByID(shippingInstructionTO.getPlaceOfIssueID()))
          .thenReturn(Mono.just(locationTO));
        when(shippingInstructionService.findById(transportDocument.getShippingInstructionReference()))
          .thenReturn(Mono.just(shippingInstructionTO));
      when(chargeService.fetchChargesByTransportDocumentReference(
              transportDocumentTO.getTransportDocumentReference()))
          .thenReturn(Flux.just(chargeTO));
      when(carrierClauseService.fetchCarrierClausesByTransportDocumentReference(
              transportDocumentTO.getTransportDocumentReference()))
          .thenReturn(Flux.just(carrierClauseTO));

      StepVerifier.create(
              transportDocumentServiceImpl.findByTransportDocumentReference(
                  "TransportDocumentReference1"))
          .assertNext(
              transportDocumentTOResponse -> {
                assertEquals(carrier.getSmdgCode(), transportDocumentTOResponse.getIssuerCode());
                assertEquals(1, transportDocumentTOResponse.getCharges().size());
                assertNotNull(transportDocumentTOResponse.getPlaceOfIssue());
                assertEquals(1, transportDocumentTOResponse.getCarrierClauses().size());
                assertEquals(
                    1, transportDocumentTOResponse.getShippingInstruction().getShipments().size());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Get transport document without charges should return transport document without charges")
    void testFindTransportDocumentWithoutCharges() {

      carrier.setNmftaCode(null);

      when(transportDocumentRepository.findById((String) any()))
          .thenReturn(Mono.just(transportDocument));
      when(carrierRepository.findById((UUID) any())).thenReturn(Mono.just(carrier));
      when(locationService.fetchLocationDeepObjByID(shippingInstructionTO.getPlaceOfIssueID()))
          .thenReturn(Mono.just(locationTO));
        when(shippingInstructionService.findById(transportDocument.getShippingInstructionReference()))
          .thenReturn(Mono.just(shippingInstructionTO));
      when(chargeService.fetchChargesByTransportDocumentReference(
              transportDocumentTO.getTransportDocumentReference()))
          .thenReturn(Flux.empty());
      when(carrierClauseService.fetchCarrierClausesByTransportDocumentReference(
              transportDocumentTO.getTransportDocumentReference()))
          .thenReturn(Flux.just(carrierClauseTO));

      StepVerifier.create(
              transportDocumentServiceImpl.findByTransportDocumentReference(
                  "TransportDocumentReference1"))
          .assertNext(
              transportDocumentTOResponse -> {
                assertEquals(carrier.getSmdgCode(), transportDocumentTOResponse.getIssuerCode());
                assertEquals(0, transportDocumentTOResponse.getCharges().size());
                assertNotNull(transportDocumentTOResponse.getPlaceOfIssue());
                assertEquals(1, transportDocumentTOResponse.getCarrierClauses().size());
                assertEquals(
                    1, transportDocumentTOResponse.getShippingInstruction().getShipments().size());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Get transport document without carrier clauses should return transport document without carrier clauses")
    void testFindTransportDocumentWithoutCarrierClauses() {

      carrier.setNmftaCode(null);

      when(transportDocumentRepository.findById((String) any()))
          .thenReturn(Mono.just(transportDocument));
      when(carrierRepository.findById((UUID) any())).thenReturn(Mono.just(carrier));
      when(locationService.fetchLocationDeepObjByID(shippingInstructionTO.getPlaceOfIssueID()))
          .thenReturn(Mono.just(locationTO));
        when(shippingInstructionService.findById(transportDocument.getShippingInstructionReference()))
          .thenReturn(Mono.just(shippingInstructionTO));
      when(chargeService.fetchChargesByTransportDocumentReference(
              transportDocumentTO.getTransportDocumentReference()))
          .thenReturn(Flux.just(chargeTO));
      when(carrierClauseService.fetchCarrierClausesByTransportDocumentReference(
              transportDocumentTO.getTransportDocumentReference()))
          .thenReturn(Flux.empty());

      StepVerifier.create(
              transportDocumentServiceImpl.findByTransportDocumentReference(
                  "TransportDocumentReference1"))
          .assertNext(
              transportDocumentTOResponse -> {
                assertEquals(carrier.getSmdgCode(), transportDocumentTOResponse.getIssuerCode());
                assertEquals(1, transportDocumentTOResponse.getCharges().size());
                assertNotNull(transportDocumentTOResponse.getPlaceOfIssue());
                assertEquals(0, transportDocumentTOResponse.getCarrierClauses().size());
                assertEquals(
                    1, transportDocumentTOResponse.getShippingInstruction().getShipments().size());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName("Get transport document without shipping instruction should return an error")
    void testFindTransportDocumentWithoutShippingInstruction() {
      when(transportDocumentRepository.findById((String) any()))
          .thenReturn(Mono.just(transportDocument));
      when(carrierRepository.findById((UUID) any())).thenReturn(Mono.just(carrier));
      when(locationService.fetchLocationDeepObjByID(shippingInstructionTO.getPlaceOfIssueID()))
          .thenReturn(Mono.just(locationTO));
        when(shippingInstructionService.findById(transportDocument.getShippingInstructionReference()))
          .thenReturn(Mono.empty());
      when(chargeService.fetchChargesByTransportDocumentReference(
              transportDocumentTO.getTransportDocumentReference()))
          .thenReturn(Flux.just(chargeTO));
      when(carrierClauseService.fetchCarrierClausesByTransportDocumentReference(
              transportDocumentTO.getTransportDocumentReference()))
          .thenReturn(Flux.just(carrierClauseTO));

      StepVerifier.create(
              transportDocumentServiceImpl.findByTransportDocumentReference(
                  "TransportDocumentReference1"))
          .expectErrorSatisfies(
              throwable -> {
                Assertions.assertTrue(throwable instanceof ConcreteRequestErrorMessageException);
                assertEquals(
                    "No shipping instruction found with shipping instruction reference: "
                        + transportDocument.getShippingInstructionReference(),
                    throwable.getMessage());
              })
          .verify();
    }

    @Test
    @DisplayName(
        "Test transportDocument without issuer carrier should return transport document without issuer")
    void testGetTransportDocumentWithNoIssuerCarrierFound() {
      when(transportDocumentRepository.findById((String) any()))
          .thenReturn(Mono.just(transportDocument));
      when(carrierRepository.findById((UUID) any())).thenReturn(Mono.empty());
      when(locationService.fetchLocationDeepObjByID(shippingInstructionTO.getPlaceOfIssueID()))
          .thenReturn(Mono.just(locationTO));
        when(shippingInstructionService.findById(transportDocument.getShippingInstructionReference()))
          .thenReturn(Mono.just(shippingInstructionTO));
      when(chargeService.fetchChargesByTransportDocumentReference(
              transportDocumentTO.getTransportDocumentReference()))
          .thenReturn(Flux.just(chargeTO));
      when(carrierClauseService.fetchCarrierClausesByTransportDocumentReference(
              transportDocumentTO.getTransportDocumentReference()))
          .thenReturn(Flux.just(carrierClauseTO));

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
                assertEquals(
                    1, transportDocumentTOResponse.getShippingInstruction().getShipments().size());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "No transport document found for transport document reference should return an empty result.")
    void testNoTransportDocumentFound() {
      when(transportDocumentRepository.findById((String) any())).thenReturn(Mono.empty());

      StepVerifier.create(
              transportDocumentServiceImpl.findByTransportDocumentReference(
                  "TransportDocumentReference1"))
          .verifyComplete();
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
      when(transportDocumentRepository.findById((String) any()))
          .thenReturn(Mono.just(transportDocument));
      when(carrierRepository.findById((UUID) any())).thenReturn(Mono.just(carrier));
      when(locationService.fetchLocationDeepObjByID(shippingInstructionTO.getPlaceOfIssueID()))
          .thenReturn(Mono.just(locationTO));
        when(shippingInstructionService.findById(transportDocument.getShippingInstructionReference()))
          .thenReturn(Mono.just(shippingInstructionTO));
      when(chargeService.fetchChargesByTransportDocumentReference(
              transportDocumentTO.getTransportDocumentReference()))
          .thenReturn(Flux.just(chargeTO));
      when(carrierClauseService.fetchCarrierClausesByTransportDocumentReference(
              transportDocumentTO.getTransportDocumentReference()))
          .thenReturn(Flux.just(carrierClauseTO));
      when(shippingInstructionRepository.setDocumentStatusByID(any(), any(), any()))
          .thenReturn(Mono.empty());
      when(bookingRepository.findAllByShippingInstructionReference(any())).thenReturn(Flux.just(booking));
      when(bookingRepository.findByCarrierBookingRequestReference(any()))
          .thenReturn(Mono.just(new Booking()));
      when(bookingRepository
              .updateDocumentStatusAndUpdatedDateTimeForCarrierBookingRequestReference(
                  any(), any(), any()))
          .thenReturn(Mono.empty());
        when(shipmentService.findByShippingInstructionReference(any()))
          .thenReturn((Mono.just(List.of(shipmentTO))));
      when(shipmentEventService.create(any())).thenReturn(Mono.just(new ShipmentEvent()));

      StepVerifier.create(
              transportDocumentServiceImpl.ApproveTransportDocument("TransportDocumentReference1"))
          .assertNext(
              transportDocumentTOResponse -> {
                assertNotNull(transportDocumentTOResponse.getShippingInstruction());
                assertNotNull(transportDocumentTOResponse.getPlaceOfIssue());
                assertEquals(
                    ShipmentEventTypeCode.APPR,
                    transportDocumentTOResponse.getShippingInstruction().getDocumentStatus());
                assertEquals(1, transportDocumentTOResponse.getCharges().size());
                assertEquals(1, transportDocumentTOResponse.getCarrierClauses().size());
                assertEquals(
                    1, transportDocumentTOResponse.getShippingInstruction().getShipments().size());
                assertEquals(
                    ShipmentEventTypeCode.CMPL,
                    transportDocumentTOResponse
                        .getShippingInstruction()
                        .getShipments()
                        .get(0)
                        .getBooking()
                        .getDocumentStatus());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "No transport document found for transport document reference should raise a mono error")
    void testNoTransportDocumentFoundToApprove() {
      when(transportDocumentRepository.findById((String) any())).thenReturn(Mono.empty());

      StepVerifier.create(
              transportDocumentServiceImpl.ApproveTransportDocument("TransportDocumentReference1"))
          .expectErrorMatches(
              throwable ->
                  throwable instanceof ConcreteRequestErrorMessageException
                      && ((ConcreteRequestErrorMessageException) throwable)
                          .getReason()
                          .equals("notFound")
                      && throwable
                          .getMessage()
                          .equals(
                              "No Transport Document found with ID: TransportDocumentReference1"))
          .verify();
    }

    @Test
    @DisplayName(
        "Approving a transport document that has a SI with invalid status document status  should raise a mono error")
    void testApproveTransportDocumentThatHasShippingInstructionInvalidDocumentStatus() {

      when(bookingRepository.findAllByShippingInstructionReference(any())).thenReturn(Flux.just(booking));
      transportDocumentTO.getShippingInstruction().setDocumentStatus(ShipmentEventTypeCode.RECE);
      assertEquals(
          transportDocumentTO.getShippingInstruction().getDocumentStatus(),
          ShipmentEventTypeCode.RECE);

      when(transportDocumentRepository.findById((String) any()))
          .thenReturn(Mono.just(transportDocument));
      when(carrierRepository.findById((UUID) any())).thenReturn(Mono.just(carrier));
      when(locationService.fetchLocationDeepObjByID(shippingInstructionTO.getPlaceOfIssueID()))
          .thenReturn(Mono.just(locationTO));
        when(shippingInstructionService.findById(transportDocument.getShippingInstructionReference()))
          .thenReturn(Mono.just(shippingInstructionTO));
      when(chargeService.fetchChargesByTransportDocumentReference(
              transportDocumentTO.getTransportDocumentReference()))
          .thenReturn(Flux.just(chargeTO));
      when(carrierClauseService.fetchCarrierClausesByTransportDocumentReference(
              transportDocumentTO.getTransportDocumentReference()))
          .thenReturn(Flux.just(carrierClauseTO));

      StepVerifier.create(
              transportDocumentServiceImpl.ApproveTransportDocument("TransportDocumentReference1"))
          .expectErrorMatches(
              throwable ->
                  throwable instanceof ConcreteRequestErrorMessageException
                      && ((ConcreteRequestErrorMessageException) throwable)
                          .getReason()
                          .equals("invalidParameter")
                      && throwable
                          .getMessage()
                          .equals(
                              "Cannot Approve Transport Document with Shipping Instruction that is not in status PENA"))
          .verify();
    }

    @Test
    @DisplayName(
        "Approving a transport document that has a SI with no shipments  should raise a mono error")
    void testApproveTransportDocumentThatHasShippingInstructionWithNoShipments() {

      when(transportDocumentRepository.findById((String) any()))
          .thenReturn(Mono.just(transportDocument));
      when(carrierRepository.findById((UUID) any())).thenReturn(Mono.just(carrier));
      when(locationService.fetchLocationDeepObjByID(shippingInstructionTO.getPlaceOfIssueID()))
          .thenReturn(Mono.just(locationTO));
        when(shippingInstructionService.findById(transportDocument.getShippingInstructionReference()))
          .thenReturn(Mono.just(shippingInstructionTO));
      when(chargeService.fetchChargesByTransportDocumentReference(
              transportDocumentTO.getTransportDocumentReference()))
          .thenReturn(Flux.just(chargeTO));
      when(carrierClauseService.fetchCarrierClausesByTransportDocumentReference(
              transportDocumentTO.getTransportDocumentReference()))
          .thenReturn(Flux.just(carrierClauseTO));
      when(shippingInstructionRepository.setDocumentStatusByID(any(), any(), any()))
          .thenReturn(Mono.empty());
        when(shipmentService.findByShippingInstructionReference(any()))
          .thenReturn((Mono.just(Collections.emptyList())));
      when(bookingRepository.findAllByShippingInstructionReference(any())).thenReturn(Flux.just(booking));

      StepVerifier.create(
              transportDocumentServiceImpl.ApproveTransportDocument("TransportDocumentReference1"))
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

      when(transportDocumentRepository.findById((String) any()))
          .thenReturn(Mono.just(transportDocument));
      when(carrierRepository.findById((UUID) any())).thenReturn(Mono.just(carrier));
      when(locationService.fetchLocationDeepObjByID(shippingInstructionTO.getPlaceOfIssueID()))
          .thenReturn(Mono.just(locationTO));
        when(shippingInstructionService.findById(transportDocument.getShippingInstructionReference()))
          .thenReturn(Mono.just(shippingInstructionTO));
      when(chargeService.fetchChargesByTransportDocumentReference(
              transportDocumentTO.getTransportDocumentReference()))
          .thenReturn(Flux.just(chargeTO));
      when(carrierClauseService.fetchCarrierClausesByTransportDocumentReference(
              transportDocumentTO.getTransportDocumentReference()))
          .thenReturn(Flux.just(carrierClauseTO));
      when(shippingInstructionRepository.setDocumentStatusByID(any(), any(), any()))
          .thenReturn(Mono.empty());
        when(shipmentService.findByShippingInstructionReference(any()))
          .thenReturn((Mono.just(List.of(shipmentTO))));
      when(bookingRepository.findByCarrierBookingRequestReference(any())).thenReturn(Mono.empty());
      when(bookingRepository.findAllByShippingInstructionReference(any())).thenReturn(Flux.just(booking));

      StepVerifier.create(
              transportDocumentServiceImpl.ApproveTransportDocument("TransportDocumentReference1"))
          .expectErrorMatches(
              throwable ->
                  throwable instanceof IllegalStateException
                      && throwable
                          .getMessage()
                          .equals(
                              "The CarrierBookingReference: "
                                  + transportDocumentTO.getTransportDocumentReference()
                                  + " specified on ShippingInstruction:"
                                  + transportDocumentTO
                                      .getShippingInstruction()
                                      .getShippingInstructionReference()
                                  + " does not exist!"))
          .verify();
    }

    @Test
    @DisplayName("Approving a transport document that fails when a shipment event is being created")
    void testApproveTransportDocumentThatFailsOnShipmentCreation() {

      when(transportDocumentRepository.findById((String) any()))
          .thenReturn(Mono.just(transportDocument));
      when(carrierRepository.findById((UUID) any())).thenReturn(Mono.just(carrier));
      when(locationService.fetchLocationDeepObjByID(shippingInstructionTO.getPlaceOfIssueID()))
          .thenReturn(Mono.just(locationTO));
        when(shippingInstructionService.findById(transportDocument.getShippingInstructionReference()))
          .thenReturn(Mono.just(shippingInstructionTO));
      when(chargeService.fetchChargesByTransportDocumentReference(
              transportDocumentTO.getTransportDocumentReference()))
          .thenReturn(Flux.just(chargeTO));
      when(carrierClauseService.fetchCarrierClausesByTransportDocumentReference(
              transportDocumentTO.getTransportDocumentReference()))
          .thenReturn(Flux.just(carrierClauseTO));
      when(shippingInstructionRepository.setDocumentStatusByID(any(), any(), any()))
          .thenReturn(Mono.empty());
      when(bookingRepository.findAllByShippingInstructionReference(any())).thenReturn(Flux.just(booking));
      when(bookingRepository.findByCarrierBookingRequestReference(any()))
          .thenReturn(Mono.just(new Booking()));
      when(bookingRepository
              .updateDocumentStatusAndUpdatedDateTimeForCarrierBookingRequestReference(
                  any(), any(), any()))
          .thenReturn(Mono.empty());
        when(shipmentService.findByShippingInstructionReference(any()))
          .thenReturn((Mono.just(List.of(shipmentTO))));
      when(shipmentEventService.create(any())).thenReturn(Mono.empty());

      StepVerifier.create(
              transportDocumentServiceImpl.ApproveTransportDocument("TransportDocumentReference1"))
          .expectErrorMatches(
              throwable ->
                  throwable instanceof ConcreteRequestErrorMessageException
                      && ((ConcreteRequestErrorMessageException) throwable)
                          .getReason()
                          .equals("internalError")
                      && throwable
                          .getMessage()
                          .equals(
                              "Failed to create shipment event for transport Document:"
                                  + transportDocumentTO.getTransportDocumentReference()))
          .verify();
    }

    @Test
    @DisplayName("Fail if carrierBookingReference linked to a Booking with invalid documentStatus")
    void testCreateShippingInstructionShouldFailWithInvalidBookingDocumentStatus() {

      String transportDocumentReference = "TransportDocumentReference1";

      when(transportDocumentRepository.findById((String) any()))
          .thenReturn(Mono.just(transportDocument));
      when(carrierRepository.findById((UUID) any())).thenReturn(Mono.just(carrier));
      when(locationService.fetchLocationDeepObjByID(shippingInstructionTO.getPlaceOfIssueID()))
          .thenReturn(Mono.just(locationTO));
      when(shippingInstructionService.findById(transportDocument.getShippingInstructionReference()))
          .thenReturn(Mono.just(shippingInstructionTO));
      when(chargeService.fetchChargesByTransportDocumentReference(
              transportDocumentTO.getTransportDocumentReference()))
          .thenReturn(Flux.just(chargeTO));
      when(carrierClauseService.fetchCarrierClausesByTransportDocumentReference(
              transportDocumentTO.getTransportDocumentReference()))
          .thenReturn(Flux.just(carrierClauseTO));

      // Test all invalid status except CONFIRMED
      for (ShipmentEventTypeCode s : ShipmentEventTypeCode.values()) {
        if (!BOOKING_DOCUMENT_STATUSES.contains(s.toString())) continue;
        if (s.equals(ShipmentEventTypeCode.CONF)) continue;

        booking.setDocumentStatus(s);
        when(bookingRepository.findAllByShippingInstructionReference(any()))
            .thenReturn(Flux.just(booking));

        StepVerifier.create(
                transportDocumentServiceImpl.ApproveTransportDocument(
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

      when(transportDocumentRepository.findById((String) any()))
          .thenReturn(Mono.just(transportDocument));
      when(carrierRepository.findById((UUID) any())).thenReturn(Mono.just(carrier));
      when(locationService.fetchLocationDeepObjByID(shippingInstructionTO.getPlaceOfIssueID()))
          .thenReturn(Mono.just(locationTO));
      when(shippingInstructionService.findById(transportDocument.getShippingInstructionReference()))
          .thenReturn(Mono.just(shippingInstructionTO));
      when(chargeService.fetchChargesByTransportDocumentReference(
              transportDocumentTO.getTransportDocumentReference()))
          .thenReturn(Flux.just(chargeTO));
      when(carrierClauseService.fetchCarrierClausesByTransportDocumentReference(
              transportDocumentTO.getTransportDocumentReference()))
          .thenReturn(Flux.just(carrierClauseTO));
      when(bookingRepository.findAllByShippingInstructionReference(any())).thenReturn(Flux.empty());

      StepVerifier.create(
              transportDocumentServiceImpl.ApproveTransportDocument(transportDocumentReference))
          .expectErrorSatisfies(
              throwable -> {
                Assertions.assertTrue(throwable instanceof ConcreteRequestErrorMessageException);
                assertEquals(
                    "No bookings found for carrier booking reference: " + transportDocumentReference,
                    throwable.getMessage());
              })
          .verify();
    }
  }
}
