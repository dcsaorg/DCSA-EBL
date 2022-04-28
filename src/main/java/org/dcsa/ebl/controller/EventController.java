package org.dcsa.ebl.controller;

import org.dcsa.core.events.controller.AbstractEventController;
import org.dcsa.core.events.model.Event;
import org.dcsa.core.events.model.enums.EventType;
import org.dcsa.core.events.util.ExtendedGenericEventRequest;
import org.dcsa.core.extendedrequest.ExtendedRequest;
import org.dcsa.core.extendedrequest.QueryFieldRestriction;
import org.dcsa.core.query.DBEntityAnalysis;
import org.dcsa.core.validator.EnumSubset;
import org.dcsa.ebl.service.EBLShipmentEventService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
      protected DBEntityAnalysis.DBEntityAnalysisBuilder<Event> prepareDBEntityAnalysis() {
        return super.prepareDBEntityAnalysis()
          // Restrict several enum fields to the subset supported by eBL
          .registerRestrictionOnQueryField("eventType", QueryFieldRestriction.enumSubset(EventType.SHIPMENT.name()))
          .registerRestrictionOnQueryField("shipmentEventTypeCode", QueryFieldRestriction.enumSubset(EBL_DOCUMENT_STATUSES))
          .registerRestrictionOnQueryField("documentTypeCode", QueryFieldRestriction.enumSubset(EBL_DOCUMENT_TYPE_CODES))
          ;
      }
    };
  }

  @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
  public Flux<Event> findAll(
      @RequestParam(value = "shipmentEventTypeCode", required = false)
          @EnumSubset(anyOf = EBL_DOCUMENT_STATUSES)
          String shipmentEventTypeCode,  // String because it is a comma separated list of values
      @RequestParam(value = "documentTypeCode", required = false)
          @EnumSubset(anyOf = EBL_DOCUMENT_TYPE_CODES)
          String documentTypeCode,  // String because it is a comma separated list of values
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
