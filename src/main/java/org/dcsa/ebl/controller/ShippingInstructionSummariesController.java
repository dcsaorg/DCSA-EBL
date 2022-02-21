package org.dcsa.ebl.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dcsa.core.events.model.ShippingInstruction;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.core.exception.ConcreteRequestErrorMessageException;
import org.dcsa.core.extendedrequest.ExtendedParameters;
import org.dcsa.core.validator.EnumSubset;
import org.dcsa.ebl.extendedrequest.ShippingInstructionSummariesExtendedRequest;
import org.dcsa.ebl.model.transferobjects.ShippingInstructionSummaryTO;
import org.dcsa.ebl.service.ShippingInstructionSummariesService;
import org.springframework.data.r2dbc.dialect.R2dbcDialect;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
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
public class ShippingInstructionSummariesController {
    private final ExtendedParameters extendedParameters;
    private final R2dbcDialect r2dbcDialect;
    private final ShippingInstructionSummariesService service;

    @GetMapping
    public Flux<ShippingInstructionSummaryTO> getBookingConfirmationSummaries(
            @RequestParam(value = "carrierBookingReference", required = false) String carrierBookingReference,
            @RequestParam(value = "documentStatus", required = false) @EnumSubset(anyOf = ShipmentEventTypeCode.EBL_DOCUMENT_STATUSES) ShipmentEventTypeCode documentStatus,
            ServerHttpRequest request) {

        log.debug("getBookingConfirmationSummaries: carrierBookingReference='{}', documentStatus='{}'", carrierBookingReference, documentStatus);

        var extendedRequest = new ShippingInstructionSummariesExtendedRequest<>(
                carrierBookingReference, extendedParameters, r2dbcDialect, ShippingInstruction.class
        );

        try {
            extendedRequest.parseParameter(request.getQueryParams());
        } catch (ConcreteRequestErrorMessageException e) {
            return Flux.error(e);
        }

        return service.findShippingInstructions(extendedRequest);
    }
}
