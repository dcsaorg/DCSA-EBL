package org.dcsa.ebl.controller;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.exception.GetException;
import org.dcsa.core.extendedrequest.ExtendedParameters;
import org.dcsa.core.extendedrequest.ExtendedRequest;
import org.dcsa.ebl.extendedrequest.ShippingInstructionExtendedRequest;
import org.dcsa.ebl.model.ShippingInstruction;
import org.dcsa.ebl.model.transferobjects.ShippingInstructionTO;
import org.dcsa.ebl.service.ShippingInstructionService;
import org.springframework.data.r2dbc.dialect.R2dbcDialect;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "shipping-instructions-summaries", produces = {MediaType.APPLICATION_JSON_VALUE})
public class ShippingInstructionsSummariesTOController extends AbstractTOController<ShippingInstructionService> {

    private final ExtendedParameters extendedParameters;

    private final ShippingInstructionService shippingInstructionTOService;

    private final R2dbcDialect r2dbcDialect;

    @Override
    public ShippingInstructionService getService() {
        return shippingInstructionTOService;
    }

    @Override
    public String getType() {
        return "ShippingInstructionsSummaries";
    }

    @GetMapping
    public Flux<ShippingInstructionTO> findAll(ServerHttpResponse response, ServerHttpRequest request) {
        ExtendedRequest<ShippingInstruction> extendedRequest = new ShippingInstructionExtendedRequest<>(extendedParameters,
                r2dbcDialect, ShippingInstruction.class);

        try {
            extendedRequest.parseParameter(request.getQueryParams());
        } catch (GetException e) {
            return Flux.error(e);
        }

        return Flux.empty();
//        return shippingInstructionTOService.findAllExtended(extendedRequest).doOnComplete(
//                () -> extendedRequest.insertHeaders(response, request)
//        );
    }
}
