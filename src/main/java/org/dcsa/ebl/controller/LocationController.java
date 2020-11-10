package org.dcsa.ebl.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.dcsa.core.controller.ExtendedBaseController;
import org.dcsa.core.exception.CreateException;
import org.dcsa.ebl.model.Location;
import org.dcsa.ebl.model.Location;
import org.dcsa.ebl.model.TransportDocument;
import org.dcsa.ebl.service.LocationService;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "locations", produces = {MediaType.APPLICATION_JSON_VALUE})
@Tag(name = "Locations", description = "The Location API")
public class LocationController extends ExtendedBaseController<LocationService, Location, UUID> {

    private final LocationService locationService;

    @Override
    public LocationService getService() {
        return locationService;
    }

    @Override
    public String getType() {
        return "Location";
    }

    @Operation(summary = "Find all Locations", description = "Finds all Locations in the database", tags = { "Location" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Location.class))))
    })
    @GetMapping
    @Override
    public Flux<Location> findAll(ServerHttpResponse response, ServerHttpRequest request) {
        return super.findAll(response, request);
    }

    @Operation(summary = "Find a Location", description = "Finds a specific Location in the database", tags = { "Location" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Location.class))))
    })
    @GetMapping
    @Override
    public Mono<Location> findById(UUID id) {
        return super.findById(id);
    }

    @Operation(summary = "Update a Location", description = "Update a Location", tags = { "Location" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Location.class))))
    })
    @PutMapping( consumes = "application/json", produces = "application/json")
    @Override
    public Mono<Location> update(UUID id, @Valid @RequestBody Location location) {
        return super.update(id, location);
    }

    @Override
    public Mono<Location> create(@Valid @RequestBody Location location) {
        return Mono.error(new CreateException("Not possible to create a Location"));
    }
}
