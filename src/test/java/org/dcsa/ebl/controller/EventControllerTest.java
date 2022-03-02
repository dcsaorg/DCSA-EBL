package org.dcsa.ebl.controller;

import org.dcsa.core.events.model.Reference;
import org.dcsa.core.events.model.ShipmentEvent;
import org.dcsa.core.events.model.enums.*;
import org.dcsa.core.exception.handler.GlobalExceptionHandler;
import org.dcsa.core.extendedrequest.ExtendedParameters;
import org.dcsa.core.extendedrequest.ExtendedRequest;
import org.dcsa.core.security.SecurityConfig;
import org.dcsa.ebl.service.EBLShipmentEventService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.r2dbc.dialect.R2dbcDialect;
import org.springframework.http.MediaType;
import org.springframework.r2dbc.core.binding.BindMarkersFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@DisplayName("Tests for Event Controller")
@ActiveProfiles("test")
@WebFluxTest(controllers = {EventController.class})
@Import(value = {GlobalExceptionHandler.class, SecurityConfig.class})
public class EventControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @MockBean
    @Qualifier("EBLShipmentEventServiceImpl")
    EBLShipmentEventService eBLShipmentEventService;

    private final String EVENTS_ENDPOINT = "/events";
    private ShipmentEvent shipmentEvent;
    private ShipmentEvent event;

    @MockBean
    ExtendedParameters extendedParameters;

    @MockBean
    R2dbcDialect r2dbcDialect;

    @MockBean
    ExtendedRequest extendedRequest;

    @BeforeEach
    void init() {
        // populate DTO with relevant objects to verify json schema returned
        event = new ShipmentEvent();
        event.setEventID(UUID.randomUUID());
        event.setEventType(EventType.SHIPMENT);
        event.setEventClassifierCode(EventClassifierCode.PLN);
        event.setEventDateTime(OffsetDateTime.now());
        event.setEventCreatedDateTime(OffsetDateTime.now());
        event.setCarrierBookingReference("DUMMY");

        shipmentEvent = new ShipmentEvent();
        shipmentEvent.setEventID(UUID.fromString("5e51e72c-d872-11ea-811c-0f8f10a32ea1"));
        shipmentEvent.setEventType(EventType.SHIPMENT);
        shipmentEvent.setEventClassifierCode(EventClassifierCode.PLN);
        shipmentEvent.setShipmentEventTypeCode(ShipmentEventTypeCode.CONF);
        shipmentEvent.setDocumentTypeCode(DocumentTypeCode.BKG);
        shipmentEvent.setDocumentID("ABC123123123");
        Reference reference = new Reference();
        reference.setReferenceType(ReferenceTypeCode.FF);
        reference.setReferenceValue("String");
        shipmentEvent.setReferences(List.of(reference));

        Mockito.when(extendedParameters.getSortParameterName()).thenReturn("sort");
        Mockito.when(extendedParameters.getPaginationPageSizeName()).thenReturn("limit");
        Mockito.when(extendedParameters.getPaginationCursorName()).thenReturn("cursor");
        Mockito.when(extendedParameters.getIndexCursorName()).thenReturn("|Offset|");
        Mockito.when(extendedParameters.getEnumSplit()).thenReturn(",");
        Mockito.when(extendedParameters.getQueryParameterAttributeSeparator()).thenReturn(",");
        Mockito.when(extendedParameters.getPaginationCurrentPageName()).thenReturn("Current-Page");
        Mockito.when(extendedParameters.getPaginationFirstPageName()).thenReturn("First-Page");
        Mockito.when(extendedParameters.getPaginationPreviousPageName()).thenReturn("Last-Page");
        Mockito.when(extendedParameters.getPaginationNextPageName()).thenReturn("Next-Page");
        Mockito.when(extendedParameters.getPaginationLastPageName()).thenReturn("Last-Page");

        Mockito.when(r2dbcDialect.getBindMarkersFactory())
                .thenReturn(BindMarkersFactory.anonymous("?"));

    }

    @Test
    @DisplayName("Creation of an event should throw not supported for any request.")
    void eventCreationShouldThrowForbiddenForAnyRequest() {
        // test to confirm that the endpoint is disabled.
        webTestClient
                .post()
                .uri(EVENTS_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(event))
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectStatus()
                .value((s) -> Assertions.assertEquals(405, s));
    }

    @Test
    @DisplayName("Updating an event should throw not supported for any request.")
    void eventUpdatingShouldThrowForbiddenForAnyRequest() {
        // test to confirm that the endpoint is disabled.
        webTestClient
                .put()
                .uri("/events/{id}", event.getEventID())
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(event))
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectStatus()
                .value((s) -> Assertions.assertEquals(405, s));
    }

    @Test
    @DisplayName("Deleting an event should throw not supported for any request.")
    void eventDeletingShouldThrowForbiddenForAnyRequest() {
        // test to confirm that the endpoint is disabled.
        webTestClient
                .delete()
                .uri("/events/{id}", event.getEventID().toString())
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectStatus()
                .value((s) -> Assertions.assertEquals(405, s));
    }

    @Test
    @DisplayName("Get events should throw bad request for incorrect shipmentEventTypeCode format.")
    void testEventsShouldFailForIncorrectShipmentEventTypeCode() {
        webTestClient
                .get()
                .uri(
                        uriBuilder ->
                                uriBuilder
                                        .path("/events")
                                        .queryParam("shipmentEventTypeCode", "ABCD,DUMMY")
                                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    @DisplayName("Get events should throw bad request for incorrect shipmentEventTypeCode subset.")
    void testEventsShouldFailForIncorrectShipmentEventTypeCodeNotSubset() {
        webTestClient
                .get()
                .uri(
                        uriBuilder ->
                                uriBuilder
                                        .path("/events")
                                        .queryParam("shipmentEventTypeCode", "CONF","RELS")
                                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }


    @Test
    @DisplayName("Get events should throw bad request for incorrect documentTypeCode format.")
    void testEventsShouldFailForIncorrectDocumentTypeCode() {
        webTestClient
                .get()
                .uri(
                        uriBuilder ->
                                uriBuilder
                                        .path("/events")  // SRM -- correct generally try test
                                        .queryParam("documentTypeCode", "ABCD,DUMMY")
                                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }
    @Test
    @DisplayName("Get events should throw bad request for incorrect DocumentTypeCode subset.")
    void testEventsShouldFailForIncorrectDocumentTypeCodeSubset() {
    webTestClient
        .get()
        .uri(
            uriBuilder ->
                uriBuilder.path("/events").queryParam("shipmentEventTypeCode", "VGM").build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isBadRequest();
    }

    @Test
    @DisplayName("Get events should throw bad request for incorrect carrierBookingReference length.")
    void testEventsShouldFailForIncorrectCarrierBookingReference() {
        webTestClient
                .get()
                .uri(
                        uriBuilder ->
                                uriBuilder
                                        .path("/events")
                                        .queryParam(
                                                "carrierBookingReference", "ABC709951ABC709951ABC709951ABC709951564")
                                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    @DisplayName(
            "Get events should throw bad request for incorrect transportDocumentReference length.")
    void testEventsShouldFailForIncorrectTransportDocumentReference() {
        webTestClient
                .get()
                .uri(
                        uriBuilder ->
                                uriBuilder
                                        .path("/events")
                                        .queryParam(
                                                "transportDocumentReference", "ABC709951ABC709951ABC709951ABC609951564")
                                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }


    @Test
    @DisplayName(
            "Get events should throw bad request for incorrect equipmentReference length.")
    void testEventsShouldFailForIncorrectEquipmentReference() {
        webTestClient
                .get()
                .uri(
                        uriBuilder ->
                                uriBuilder
                                        .path("/events")
                                        .queryParam(
                                                "equipmentReference", "ABC709951ABC709951ABC709951ABC609951564")
                                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }
    //--------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("Get events should throw bad request if limit is zero.")
    void testEventsShouldFailForIncorrectLimit() {
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/events").queryParam("limit", 0).build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    @DisplayName("Get events should return list of shipment events for valid request.")
    void testEventsShouldReturnShipmentEvents() {

        Mockito.when(eBLShipmentEventService.findAllExtended(Mockito.any())).thenReturn(Flux.just(shipmentEvent));

        webTestClient
                .get()
                .uri("/events")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.[0].eventID")
                .isEqualTo(shipmentEvent.getEventID().toString())
                .jsonPath("$.[0].eventType")
                .isEqualTo(shipmentEvent.getEventType().toString());
    }

}
