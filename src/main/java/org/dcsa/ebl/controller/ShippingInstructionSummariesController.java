package org.dcsa.ebl.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dcsa.core.controller.AsymmetricQueryController;
import org.dcsa.core.events.model.ShippingInstruction;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.core.extendedrequest.ExtendedParameters;
import org.dcsa.core.extendedrequest.ExtendedRequest;
import org.dcsa.core.validator.EnumSubset;
import org.dcsa.ebl.extendedrequest.ShippingInstructionSummariesExtendedRequest;
import org.dcsa.ebl.model.transferobjects.ShippingInstructionSummaryTO;
import org.dcsa.ebl.service.impl.ShippingInstructionSummariesServiceImpl;
import org.springframework.data.r2dbc.dialect.R2dbcDialect;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "shipping-instructions-summaries", produces = {MediaType.APPLICATION_JSON_VALUE})
public class ShippingInstructionSummariesController extends AsymmetricQueryController<ShippingInstructionSummariesServiceImpl, ShippingInstruction, ShippingInstructionSummaryTO, String> {
  private final ExtendedParameters extendedParameters;
  private final R2dbcDialect r2dbcDialect;
  private final ShippingInstructionSummariesServiceImpl service;

  @GetMapping
  public Flux<ShippingInstructionSummaryTO> findShippingInstructionSummaries(
    @RequestParam(value = "carrierBookingReference", required = false) String carrierBookingReference,
    @RequestParam(value = "documentStatus", required = false) @EnumSubset(anyOf = ShipmentEventTypeCode.EBL_DOCUMENT_STATUSES) ShipmentEventTypeCode documentStatus,
    ServerHttpResponse response, ServerHttpRequest request
  ) {
    log.debug("findShippingInstructionSummaries: carrierBookingReference='{}', documentStatus='{}'", carrierBookingReference, documentStatus);
    return super.findAll(response, request);
  }

  @Override
  protected ExtendedRequest<ShippingInstruction> newExtendedRequest() {
    return new ShippingInstructionSummariesExtendedRequest(extendedParameters, r2dbcDialect);
  }

  @Override
  protected ShippingInstructionSummariesServiceImpl getService() {
    return service;
  }
}
