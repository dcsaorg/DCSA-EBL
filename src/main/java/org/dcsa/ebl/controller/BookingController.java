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
import org.dcsa.ebl.model.Booking;
import org.dcsa.ebl.service.BookingService;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "bookings", produces = {MediaType.APPLICATION_JSON_VALUE})
@Tag(name = "Bookings", description = "The Booking API")
public class BookingController extends ExtendedBaseController<BookingService, Booking, String> {

    private final BookingService bookingService;

    @Override
    public BookingService getService() {
        return bookingService;
    }

    @Override
    public String getType() {
        return "Booking";
    }

    @Operation(summary = "Find all Bookings", description = "Finds all Bookings in the database", tags = { "Booking" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Booking.class))))
    })
    @GetMapping
    @Override
    public Flux<Booking> findAll(ServerHttpResponse response, ServerHttpRequest request) {
        return super.findAll(response, request);
    }

    @Operation(summary = "Find a Booking", description = "Finds a specific Booking in the database", tags = { "Booking" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Booking.class))))
    })
    @GetMapping(path = "{bookingReference}")
    @Override
    public Mono<Booking> findById(@PathVariable String bookingReference) {
        return super.findById(bookingReference);
    }

    @Operation(summary = "Update a Booking", description = "Update a Booking", tags = { "Booking" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Booking.class))))
    })
    @PutMapping( path = "{bookingReference}", consumes = "application/json", produces = "application/json")
    @Override
    public Mono<Booking> update(@PathVariable String bookingReference, @Valid @RequestBody Booking booking) {
        return super.update(bookingReference, booking);
    }

    @Override
    @PostMapping
    public Mono<Booking> create(@Valid @RequestBody Booking booking) {
        return Mono.error(new CreateException("Not possible to create an Booking"));
    }
}
