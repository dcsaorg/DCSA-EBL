package org.dcsa.ebl.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.core.validator.EnumSubset;
import org.dcsa.ebl.model.transferobjects.ShippingInstructionSummaryTO;
import org.dcsa.ebl.service.ShippingInstructionService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import javax.validation.constraints.Min;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "shipping-instructions-summaries", produces = {MediaType.APPLICATION_JSON_VALUE})
public class ShippingInstructionSummariesController {

    private final ShippingInstructionService service;

    @GetMapping
    public Flux<ShippingInstructionSummaryTO> getBookingConfirmationSummaries(
            @RequestParam(value = "carrierBookingReference", required = false) String carrierBookingReference,
            @RequestParam(value = "documentStatus", required = false) @EnumSubset(anyOf = ShipmentEventTypeCode.EBL_DOCUMENT_STATUSES) ShipmentEventTypeCode documentStatus,
            @RequestParam(value = "limit", defaultValue = "${pagination.defaultPageSize}", required = false) @Min(1) int limit,
            @RequestParam(value = "cursor", required = false) String cursor,
            @RequestParam(value = "sort", required = false) String[] sort,
            ServerHttpResponse response) {

        log.debug("getBookingConfirmationSummaries: carrierBookingReference='{}', documentStatus='{}'", carrierBookingReference, documentStatus);
        // Pagination pagination = new Pagination(Sort.by(Sort.Direction.DESC, "shipmentCreatedDateTime"));
        // PageRequest pageRequest = pagination.createPageRequest(limit, cursor, sort);

        List<String> carrierBookingReferences = carrierBookingReference != null ? Arrays.asList(carrierBookingReference.split(",")) : Collections.emptyList();
        return service.findShippingInstructions(carrierBookingReferences, documentStatus, PageRequest.of(0, 20));
    }
}
