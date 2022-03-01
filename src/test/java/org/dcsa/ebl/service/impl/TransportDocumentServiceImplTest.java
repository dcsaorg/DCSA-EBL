package org.dcsa.ebl.service.impl;

import org.dcsa.core.events.model.*;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.core.events.model.enums.WeightUnit;
import org.dcsa.core.events.repository.CarrierRepository;
import org.dcsa.core.events.repository.ShipmentRepository;
import org.dcsa.core.events.repository.TransportDocumentRepository;
import org.dcsa.core.exception.ConcreteRequestErrorMessageException;
import org.dcsa.ebl.model.mappers.TransportDocumentMapper;
import org.dcsa.ebl.repository.ShippingInstructionRepository;
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
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests for TransportDocument Implementation.")
class TransportDocumentServiceImplTest {

  @Mock TransportDocumentRepository transportDocumentRepository;
  @Mock ShipmentRepository shipmentRepository;
  @Mock CarrierRepository carrierRepository;
  @Mock ShippingInstructionRepository shippingInstructionRepository;

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

  @BeforeEach
  void init() {
    initEntities();
  }

  private void initEntities() {
    OffsetDateTime now = OffsetDateTime.now();
    carrier = new Carrier();
    carrier.setId(UUID.randomUUID());
    carrier.setNmftaCode("x".repeat(4));
    carrier.setSmdgCode(null);
    carrier.setCarrierName("Klods-Hans");

    shippingInstruction = new ShippingInstruction();
    shippingInstruction.setShippingInstructionID(UUID.randomUUID().toString());
    shippingInstruction.setDocumentStatus(ShipmentEventTypeCode.RECE);
    shippingInstruction.setShippingInstructionCreatedDateTime(now);
    shippingInstruction.setShippingInstructionUpdatedDateTime(now);

    transportDocument = new TransportDocument();
    transportDocument.setTransportDocumentReference("x".repeat(20));
    transportDocument.setShippingInstructionID(shippingInstruction.getShippingInstructionID());
    transportDocument.setIssuer(carrier.getId());
    transportDocument.setIssueDate(LocalDate.now());
    transportDocument.setTransportDocumentRequestCreatedDateTime(now);
    transportDocument.setTransportDocumentRequestUpdatedDateTime(now);
    transportDocument.setDeclaredValue(12f);
    transportDocument.setDeclaredValueCurrency("DKK");
    transportDocument.setReceivedForShipmentDate(LocalDate.now());

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
    cargoItem.setShippingInstructionID(shippingInstruction.getShippingInstructionID());
  }

  @Nested
  @DisplayName("Tests for the method findById(#TransportDocumentID)")
  class GetTransportDocumentSummaryTest {

    @Test
    @DisplayName("Test GET shipping instruction with everything for a valid ID.")
    void testGetTransportDocumentWithEverythingForValidID() {
      when(carrierRepository.findById(any(UUID.class))).thenReturn(Mono.just(carrier));
      when(shippingInstructionRepository.findById(any(String.class))).thenReturn(Mono.just(shippingInstruction));
      when(shippingInstructionRepository.findCarrierBookingReferenceByShippingInstructionID(any())).thenReturn(Flux.just(shipment.getCarrierBookingReference()));

      StepVerifier.create(transportDocumentServiceImpl.mapDM2TO(transportDocument))
          .assertNext(
              result -> {
                assertNotNull(result.getTransportDocumentReference());
                assertNotNull(result.getIssuerCode());
                assertNotNull(result.getIssueDate());
                assertNotNull(result.getTransportDocumentRequestCreatedDateTime());
                assertNotNull(result.getTransportDocumentRequestUpdatedDateTime());
                assertNotNull(result.getShippingInstructionID());
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
      when(shippingInstructionRepository.findById(any(String.class))).thenReturn(Mono.just(shippingInstruction));
      when(shippingInstructionRepository.findCarrierBookingReferenceByShippingInstructionID(any())).thenReturn(Flux.empty());

      StepVerifier.create(transportDocumentServiceImpl.mapDM2TO(transportDocument))
          .assertNext(
              result -> {
                assertNotNull(result.getTransportDocumentReference());
                assertNotNull(result.getIssuerCode());
                assertNotNull(result.getIssueDate());
                assertNotNull(result.getTransportDocumentRequestCreatedDateTime());
                assertNotNull(result.getTransportDocumentRequestUpdatedDateTime());
                assertNotNull(result.getShippingInstructionID());
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

      when(shippingInstructionRepository.findById(any(String.class))).thenReturn(Mono.just(shippingInstruction));
      when(shippingInstructionRepository.findCarrierBookingReferenceByShippingInstructionID(any())).thenReturn(Flux.just(shipment.getCarrierBookingReference()));

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

      when(shippingInstructionRepository.findById(any(String.class))).thenReturn(Mono.just(shippingInstruction));
      when(shippingInstructionRepository.findCarrierBookingReferenceByShippingInstructionID(any())).thenReturn(Flux.just(shipment.getCarrierBookingReference()));
      when(carrierRepository.findById(any(UUID.class))).thenReturn(Mono.empty());

      StepVerifier.create(
              transportDocumentServiceImpl.mapDM2TO(transportDocument))
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

      StepVerifier.create(
              transportDocumentServiceImpl.mapDM2TO(transportDocument))
          .expectErrorSatisfies(
              throwable -> {
                Assertions.assertTrue(throwable instanceof ConcreteRequestErrorMessageException);
                assertEquals(
                    "No shipping instruction was found with ID: " + transportDocument.getShippingInstructionID(),
                    throwable.getMessage());
              })
          .verify();
    }
  }
}
