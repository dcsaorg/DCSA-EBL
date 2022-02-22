package org.dcsa.ebl.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.core.validator.EnumSubset;
import org.dcsa.ebl.model.TransportDocumentSummary;
import org.dcsa.ebl.model.transferobjects.ShippingInstructionSummaryTO;
import org.dcsa.ebl.model.transferobjects.TransportDocumentTO;
import org.dcsa.ebl.repository.ShippingInstructionRepository;
import org.dcsa.ebl.service.TransportDocumentService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.constraints.Min;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping(value = "transport-document-summaries", produces = {MediaType.APPLICATION_JSON_VALUE})
public class TransportDocumentSummariesController {

    private final TransportDocumentService transportDocumentService;

    @GetMapping
    public Mono<List<TransportDocumentSummary>> getBookingConfirmationSummaries(
            @RequestParam(value = "carrierBookingReference", required = false) String carrierBookingReference,
            @RequestParam(value = "documentStatus", required = false) @EnumSubset(anyOf = ShipmentEventTypeCode.EBL_DOCUMENT_STATUSES) ShipmentEventTypeCode documentStatus,
            @RequestParam(value = "limit", defaultValue = "${pagination.defaultPageSize}", required = false) @Min(1) int limit,
            @RequestParam(value = "cursor", required = false) String cursor,
            @RequestParam(value = "sort", required = false) String[] sort,
            ServerHttpResponse response) {

        List<String> carrierBookingReferences = carrierBookingReference != null ? Arrays.stream(carrierBookingReference.split(",")).map(String::trim).collect(Collectors.toList()) : Collections.emptyList();
        return transportDocumentService.findByCarrierBookingReference(carrierBookingReferences, documentStatus, PageRequest.of(0, 20));
    }
}
