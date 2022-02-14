package org.dcsa.ebl.controller;

import org.dcsa.core.events.controller.AbstractEventController;
import org.dcsa.core.events.model.Event;
import org.dcsa.core.events.model.enums.DocumentTypeCode;
import org.dcsa.core.events.model.enums.EventType;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.core.events.util.ExtendedGenericEventRequest;
import org.dcsa.core.extendedrequest.ExtendedRequest;
import org.dcsa.core.validator.EnumSubset;
import org.dcsa.ebl.service.EBLShipmentEventService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.util.*;

import static org.dcsa.core.events.model.enums.DocumentTypeCode.EBL_DOCUMENT_TYPE_CODES;
import static org.dcsa.core.events.model.enums.ShipmentEventTypeCode.EBL_DOCUMENT_STATUSES;

@RestController
@Validated
@RequestMapping(
    value = "events",
    produces = {MediaType.APPLICATION_JSON_VALUE})
public class EventController extends AbstractEventController<EBLShipmentEventService, Event> {

  private final EBLShipmentEventService EBLShipmentEventService;

  public EventController(
      @Qualifier("EBLShipmentEventServiceImpl") EBLShipmentEventService EBLShipmentEventService) {
    this.EBLShipmentEventService = EBLShipmentEventService;
  }

  @Override
  public EBLShipmentEventService getService() {
    return EBLShipmentEventService;
  }

  @Override
  protected ExtendedRequest<Event> newExtendedRequest() {
    return new ExtendedGenericEventRequest(extendedParameters, r2dbcDialect) {
      @Override
      public void parseParameter(Map<String, List<String>> params) {
        Map<String, List<String>> p = new HashMap<>(params);
        // Add the eventType parameter (if it is missing) in order to limit the resultset
        // to *only* SHIPMENT events
        p.putIfAbsent("eventType", List.of(EventType.SHIPMENT.name()));
        // to *only* allow EBL  ShipmentEventTypeCode subset.
        p.putIfAbsent("shipmentEventTypeCode", Collections.singletonList(EBL_DOCUMENT_STATUSES));
        // to *only* allowed EBL ShipmentEventTypeCode subset.
        p.putIfAbsent("documentTypeCode", Collections.singletonList(EBL_DOCUMENT_TYPE_CODES));
        super.parseParameter(p);
      }
    };
  }

  @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
  public Flux<Event> findAll(
      @RequestParam(value = "shipmentEventTypeCode", required = false)
          @EnumSubset(anyOf = EBL_DOCUMENT_STATUSES)
          ShipmentEventTypeCode shipmentEventTypeCode,
      @RequestParam(value = "documentTypeCode", required = false)
          @EnumSubset(anyOf = EBL_DOCUMENT_TYPE_CODES)
          DocumentTypeCode documentTypeCode,
      @RequestParam(value = "carrierBookingReference", required = false) @Size(max = 35)
          String carrierBookingReference,
      @RequestParam(value = "carrierBookingRequestReference", required = false)
          String carrierBookingRequestReference,
      @RequestParam(value = "transportDocumentReference", required = false) @Size(max = 20)
          String transportDocumentReference,
      @RequestParam(value = "equipmentReference", required = false) @Size(max = 15)
          String equipmentReference,
      @RequestParam(value = "limit", defaultValue = "20", required = false) @Min(1) int limit,
      ServerHttpResponse response,
      ServerHttpRequest request) {
    return super.findAll(response, request);
  }
}
