package org.dcsa.ebl.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.dcsa.core.events.controller.AbstractEventController;
import org.dcsa.core.events.model.ShipmentEvent;
import org.dcsa.core.events.service.ShipmentEventService;
import org.dcsa.core.extendedrequest.ExtendedRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "events", produces = {MediaType.APPLICATION_JSON_VALUE})
@Tag(name = "Events", description = "The event API")
public class EventController extends AbstractEventController<ShipmentEventService, ShipmentEvent> {

    private final ShipmentEventService eventService;

    @Override
    public ShipmentEventService getService() {
        return eventService;
    }

    @Override
    protected ExtendedRequest<ShipmentEvent> newExtendedRequest() {
        return new ExtendedRequest<>(extendedParameters, r2dbcDialect, ShipmentEvent.class);
    }
}
