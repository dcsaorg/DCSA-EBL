package org.dcsa.ebl.service.impl;

import org.dcsa.core.events.edocumentation.model.transferobject.BookingTO;
import org.dcsa.core.events.edocumentation.model.transferobject.CarrierClauseTO;
import org.dcsa.core.events.edocumentation.model.transferobject.ChargeTO;
import org.dcsa.core.events.edocumentation.model.transferobject.ShipmentTO;
import org.dcsa.core.events.edocumentation.service.CarrierClauseService;
import org.dcsa.core.events.edocumentation.service.ChargeService;
import org.dcsa.core.events.edocumentation.service.ShipmentService;
import org.dcsa.core.events.model.Address;
import org.dcsa.core.events.model.Carrier;
import org.dcsa.core.events.model.ShipmentEvent;
import org.dcsa.core.events.model.TransportDocument;
import org.dcsa.core.events.model.enums.CarrierCodeListProvider;
import org.dcsa.core.events.model.enums.PaymentTerm;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
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

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests for Transport document implementation.")
class TransportDocumentServiceImplTest {

  @Mock
  TransportDocumentService transportDocumentServiceMock;
  @Mock
  TransportDocumentServiceImpl transportDocumentServiceImplMock;
  @Mock TransportDocumentRepository transportDocumentRepository;
  @Mock CarrierRepository carrierRepository;
  @Mock ShippingInstructionService shippingInstructionService;
  @Mock ChargeService chargeService;
  @Mock CarrierClauseService carrierClauseService;
  @Mock LocationService locationService;
  @Mock ShippingInstructionRepository shippingInstructionRepository;
  @Mock BookingRepository bookingRepository;
  @Mock ShipmentService shipmentService;
  @Mock ShipmentEventService shipmentEventService;


  @Spy
  TransportDocumentMapper transportDocumentMapper =
      Mappers.getMapper(TransportDocumentMapper.class);

  @InjectMocks TransportDocumentServiceImpl transportDocumentService;

  TransportDocument transportDocument;
  Carrier carrier;

  TransportDocumentTO transportDocumentTO;
  ShippingInstructionTO shippingInstructionTO;
  ChargeTO chargeTO;
  CarrierClauseTO carrierClauseTO;
  LocationTO locationTO;
  ShipmentTO shipmentTO;
  BookingTO bookingTO;

  @BeforeEach
  void init() {

    transportDocument = new TransportDocument();
    transportDocument.setTransportDocumentReference("TransportDocumentReference1");
    transportDocument.setPlaceOfIssue("1");
    transportDocument.setIssuer(UUID.randomUUID());
    transportDocument.setShippingInstructionID("shippingInstructionID");

    carrier = new Carrier();
    carrier.setId(transportDocument.getIssuer());
    carrier.setNmftaCode("NMFT");
    carrier.setSmdgCode("SMD");

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
    chargeTO.setChargeTypeCode("chargeTypeCode");
    chargeTO.setCalculationBasis("CalculationBasics");
    chargeTO.setCurrencyAmount(100.0);
    chargeTO.setCurrencyCode("EUR");
    chargeTO.setQuantity(1.0);
    chargeTO.setUnitPrice(100.0);
    chargeTO.setPaymentTermCode(PaymentTerm.COL);

    carrierClauseTO = new CarrierClauseTO();
    carrierClauseTO.setClauseContent("CarrierClause");

    shippingInstructionTO = new ShippingInstructionTO();
    shippingInstructionTO.setIsShippedOnboardType(true);
    shippingInstructionTO.setIsElectronic(true);
    shippingInstructionTO.setIsToOrder(true);
    shippingInstructionTO.setShippingInstructionID(UUID.randomUUID().toString());
    shippingInstructionTO.setDocumentStatus(ShipmentEventTypeCode.PENA);
    shippingInstructionTO.setPlaceOfIssueID(locationTO.getId());
    shippingInstructionTO.setAreChargesDisplayedOnCopies(true);

    bookingTO = new BookingTO();
    bookingTO.setDocumentStatus(ShipmentEventTypeCode.PENA);
    shipmentTO = new ShipmentTO();
    shipmentTO.setBooking(bookingTO);
    shippingInstructionTO.setShipments(List.of(shipmentTO));

    transportDocumentTO = new TransportDocumentTO();
    transportDocumentTO.setCharges(List.of(chargeTO));
    transportDocumentTO.setPlaceOfIssue(locationTO);
    transportDocumentTO.setCarrierClauses(List.of(carrierClauseTO));
    transportDocumentTO.setShippingInstruction(shippingInstructionTO);
    transportDocumentTO.setTransportDocumentReference("TransportDocumentReference1");
  }

  @Test
  @DisplayName("Get transportdocument with reference should return transport document")
  void testFindTransportDocument() {
    when(transportDocumentRepository.findById((String) any()))
        .thenReturn(Mono.just(transportDocument));
    when(carrierRepository.findById((UUID) any())).thenReturn(Mono.just(carrier));
    when(locationService.fetchLocationDeepObjByID(shippingInstructionTO.getPlaceOfIssueID()))
        .thenReturn(Mono.just(locationTO));
    when(shippingInstructionService.findById(transportDocument.getShippingInstructionID()))
        .thenReturn(Mono.just(shippingInstructionTO));
    when(chargeService.fetchChargesByTransportDocumentReference(
            transportDocumentTO.getTransportDocumentReference()))
        .thenReturn(Flux.just(chargeTO));
    when(carrierClauseService.fetchCarrierClausesByTransportDocumentReference(
            transportDocumentTO.getTransportDocumentReference()))
        .thenReturn(Flux.just(carrierClauseTO));

    StepVerifier.create(
            transportDocumentService.findByTransportDocumentReference(
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
  @DisplayName("Get transportdocument without place of issue should return transport document without place of issue")
  void testFindTransportDocumentWithoutPlaceOfIssue() {
    transportDocument.setPlaceOfIssue(null);
    when(transportDocumentRepository.findById((String) any()))
      .thenReturn(Mono.just(transportDocument));
    when(carrierRepository.findById((UUID) any())).thenReturn(Mono.just(carrier));
    when(locationService.fetchLocationDeepObjByID(any()))
      .thenReturn(Mono.empty());
    when(shippingInstructionService.findById(transportDocument.getShippingInstructionID()))
      .thenReturn(Mono.just(shippingInstructionTO));
    when(chargeService.fetchChargesByTransportDocumentReference(
      transportDocumentTO.getTransportDocumentReference()))
      .thenReturn(Flux.just(chargeTO));
    when(carrierClauseService.fetchCarrierClausesByTransportDocumentReference(
      transportDocumentTO.getTransportDocumentReference()))
      .thenReturn(Flux.just(carrierClauseTO));

    StepVerifier.create(
        transportDocumentService.findByTransportDocumentReference(
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
  @DisplayName("Get transportdocument without top level carrierBookingReferences should return transport document without carrierBookingReferences on top level.")
  void testFindTransportDocumentWithoutCarrierBookingReferences() {
    when(transportDocumentRepository.findById((String) any()))
      .thenReturn(Mono.just(transportDocument));
    when(carrierRepository.findById((UUID) any())).thenReturn(Mono.just(carrier));
    when(locationService.fetchLocationDeepObjByID(shippingInstructionTO.getPlaceOfIssueID()))
      .thenReturn(Mono.just(locationTO));
    when(shippingInstructionService.findById(transportDocument.getShippingInstructionID()))
      .thenReturn(Mono.just(shippingInstructionTO));
    when(chargeService.fetchChargesByTransportDocumentReference(
      transportDocumentTO.getTransportDocumentReference()))
      .thenReturn(Flux.just(chargeTO));
    when(carrierClauseService.fetchCarrierClausesByTransportDocumentReference(
      transportDocumentTO.getTransportDocumentReference()))
      .thenReturn(Flux.just(carrierClauseTO));

    StepVerifier.create(
        transportDocumentService.findByTransportDocumentReference(
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
  @DisplayName("Get transportdocument without charges should return transport document without charges")
  void testFindTransportDocumentWithoutCharges() {
    when(transportDocumentRepository.findById((String) any()))
      .thenReturn(Mono.just(transportDocument));
    when(carrierRepository.findById((UUID) any())).thenReturn(Mono.just(carrier));
    when(locationService.fetchLocationDeepObjByID(shippingInstructionTO.getPlaceOfIssueID()))
      .thenReturn(Mono.just(locationTO));
    when(shippingInstructionService.findById(transportDocument.getShippingInstructionID()))
      .thenReturn(Mono.just(shippingInstructionTO));
    when(chargeService.fetchChargesByTransportDocumentReference(
      transportDocumentTO.getTransportDocumentReference()))
      .thenReturn(Flux.empty());
    when(carrierClauseService.fetchCarrierClausesByTransportDocumentReference(
      transportDocumentTO.getTransportDocumentReference()))
      .thenReturn(Flux.just(carrierClauseTO));

    StepVerifier.create(
        transportDocumentService.findByTransportDocumentReference(
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
  @DisplayName("Get transportdocument without carrier clauses should return transport document without carrier clauses")
  void testFindTransportDocumentWithoutCarrierClauses() {
    when(transportDocumentRepository.findById((String) any()))
      .thenReturn(Mono.just(transportDocument));
    when(carrierRepository.findById((UUID) any())).thenReturn(Mono.just(carrier));
    when(locationService.fetchLocationDeepObjByID(shippingInstructionTO.getPlaceOfIssueID()))
      .thenReturn(Mono.just(locationTO));
    when(shippingInstructionService.findById(transportDocument.getShippingInstructionID()))
      .thenReturn(Mono.just(shippingInstructionTO));
    when(chargeService.fetchChargesByTransportDocumentReference(
      transportDocumentTO.getTransportDocumentReference()))
      .thenReturn(Flux.just(chargeTO));
    when(carrierClauseService.fetchCarrierClausesByTransportDocumentReference(
      transportDocumentTO.getTransportDocumentReference()))
      .thenReturn(Flux.empty());

    StepVerifier.create(
        transportDocumentService.findByTransportDocumentReference(
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
  @DisplayName("Get transportdocument without shipping instruction should return an error")
  void testFindTransportDocumentWithoutShippingInstruction() {
    when(transportDocumentRepository.findById((String) any()))
      .thenReturn(Mono.just(transportDocument));
    when(carrierRepository.findById((UUID) any())).thenReturn(Mono.just(carrier));
    when(locationService.fetchLocationDeepObjByID(shippingInstructionTO.getPlaceOfIssueID()))
      .thenReturn(Mono.just(locationTO));
    when(shippingInstructionService.findById(transportDocument.getShippingInstructionID()))
      .thenReturn(Mono.empty());
    when(chargeService.fetchChargesByTransportDocumentReference(
      transportDocumentTO.getTransportDocumentReference()))
      .thenReturn(Flux.just(chargeTO));
    when(carrierClauseService.fetchCarrierClausesByTransportDocumentReference(
      transportDocumentTO.getTransportDocumentReference()))
      .thenReturn(Flux.just(carrierClauseTO));

    StepVerifier.create(
        transportDocumentService.findByTransportDocumentReference(
          "TransportDocumentReference1"))
      .expectErrorSatisfies(
        throwable -> {
          Assertions.assertTrue(throwable instanceof ConcreteRequestErrorMessageException);
          assertEquals(
            "No shipping instruction found with shipping instruction id: "
              + transportDocument.getShippingInstructionID(),
            throwable.getMessage());
        })
      .verify();
  }

  @Test
  @DisplayName("Test transportDocument without issuer carrier should return transport document without issuer")
  void testGetTransportDocumentWithNoIssuerCarrierFound() {
    when(transportDocumentRepository.findById((String) any()))
      .thenReturn(Mono.just(transportDocument));
    when(carrierRepository.findById((UUID) any())).thenReturn(Mono.empty());
    when(locationService.fetchLocationDeepObjByID(shippingInstructionTO.getPlaceOfIssueID()))
      .thenReturn(Mono.just(locationTO));
    when(shippingInstructionService.findById(transportDocument.getShippingInstructionID()))
      .thenReturn(Mono.just(shippingInstructionTO));
    when(chargeService.fetchChargesByTransportDocumentReference(
      transportDocumentTO.getTransportDocumentReference()))
      .thenReturn(Flux.just(chargeTO));
    when(carrierClauseService.fetchCarrierClausesByTransportDocumentReference(
      transportDocumentTO.getTransportDocumentReference()))
      .thenReturn(Flux.just(carrierClauseTO));

    StepVerifier.create(
        transportDocumentService.findByTransportDocumentReference(
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
  @DisplayName("No transport document found for transport document reference should return an empty result.")
  void testNoTransportDocumentFound() {
    when(transportDocumentRepository.findById((String) any()))
      .thenReturn(Mono.empty());

    StepVerifier.create(
        transportDocumentService.findByTransportDocumentReference(
          "TransportDocumentReference1"))
      .verifyComplete();
  }

  @Test
  @DisplayName("Test set SMDG code as Issuer on Transport document")
  void testSetSMDGCodeOnTransportDocument() {
    Carrier carrier = new Carrier();
    carrier.setSmdgCode("123");
    TransportDocumentTO transportDocumentTO = new TransportDocumentTO();
    transportDocumentService.setIssuerOnTransportDocument(transportDocumentTO, carrier);
    assertEquals(carrier.getSmdgCode(), transportDocumentTO.getIssuerCode());
    assertEquals(CarrierCodeListProvider.SMDG, transportDocumentTO.getIssuerCodeListProvider());
  }

  @Test
  @DisplayName("Test set NMFTA code as Issuer on Transport document")
  void testSetNMFTOCodeOnTransportDocument() {
    Carrier carrier = new Carrier();
    carrier.setNmftaCode("abcd");
    TransportDocumentTO transportDocumentTO = new TransportDocumentTO();
    transportDocumentService.setIssuerOnTransportDocument(transportDocumentTO, carrier);
    assertEquals(carrier.getNmftaCode(), transportDocumentTO.getIssuerCode());
    assertEquals(CarrierCodeListProvider.NMFTA, transportDocumentTO.getIssuerCodeListProvider());
  }

  @Test
  @DisplayName("Test unable to set issuer on TransportDocument")
  void testNoIssuerOnTransportDocument() {
    Carrier carrier = new Carrier();
    TransportDocumentTO transportDocumentTO = new TransportDocumentTO();
    transportDocumentService.setIssuerOnTransportDocument(transportDocumentTO, carrier);
    assertNull(transportDocumentTO.getIssuerCode());
  }

  @Test
  @Disabled
  @DisplayName("Approve at transport document with valid reference should return transport document with SI & bookings " +
    "document statuses set to APPR & CMPL respectively")
  void testApproveTransportDocument() {
    when(transportDocumentServiceMock.findByTransportDocumentReference("TransportDocumentReference1"))
      .thenReturn(Mono.just(transportDocumentTO));
   // when(.createShipmentEventFromTransportDocumentTO(transportDocumentTO)).thenReturn(Mono.empty());
  //  when(transportDocumentService.shipmentEventFromTransportDocumentTO(transportDocumentTO)).thenReturn(Mono.just(new ShipmentEvent()));
    when(shippingInstructionRepository.setDocumentStatusByID(any(),any(),any())).thenReturn(Mono.empty());
    when(bookingRepository.updateDocumentStatusAndUpdatedDateTimeForCarrierBookingRequestReference(any(),any(),any())).thenReturn(Mono.empty());
    when(shipmentService.findByShippingInstructionID(any())).thenReturn((Mono.just( List.of(shipmentTO))));
    when(shipmentEventService.create(any())).thenReturn(Mono.just(new ShipmentEvent()));
    // when

    StepVerifier.create(
            transportDocumentService.ApproveTransportDocument("TransportDocumentReference1"))
      //  .consumeNextWith(System.out::println)
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


}
