package org.dcsa.ebl.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.dcsa.core.controller.ExtendedBaseController;
import org.dcsa.core.exception.CreateException;
import org.dcsa.core.exception.DeleteException;
import org.dcsa.core.exception.UpdateException;
import org.dcsa.ebl.model.Location;
import org.dcsa.ebl.service.LocationService;
import org.springframework.http.HttpStatus;
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

    @GetMapping
    @Override
    public Flux<Location> findAll(ServerHttpResponse response, ServerHttpRequest request) {
        return super.findAll(response, request);
    }

    @GetMapping( path = "{locationID}" )
    @Override
    public Mono<Location> findById(@PathVariable UUID locationID) {
        return super.findById(locationID);
    }

    @PostMapping
    @Override
    public Mono<Location> create(@Valid @RequestBody Location location) {
        return Mono.error(new CreateException("Not possible to create a Location"));
    }

    @PutMapping( path = "{locationID}")
    @Override
    public Mono<Location> update(@PathVariable UUID locationID, @Valid @RequestBody Location location) {
        return Mono.error(new UpdateException("Not possible to update a Location"));
    }

    @DeleteMapping
    @ResponseStatus( HttpStatus.NO_CONTENT )
    @Override
    public Mono<Void> delete(@RequestBody Location location) {
        return Mono.error(new DeleteException("Not possible to delete a Location"));
    }

    @DeleteMapping( path ="{locationID}" )
    @ResponseStatus( HttpStatus.NO_CONTENT )
    @Override
    public Mono<Void> deleteById(@PathVariable UUID locationID) {
        return Mono.error(new DeleteException("Not possible to delete a Location"));
    }
}
