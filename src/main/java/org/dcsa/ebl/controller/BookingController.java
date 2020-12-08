package org.dcsa.ebl.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.dcsa.core.controller.ExtendedBaseController;
import org.dcsa.core.exception.CreateException;
import org.dcsa.core.exception.DeleteException;
import org.dcsa.core.exception.UpdateException;
import org.dcsa.ebl.model.transferobjects.BookingTO;
import org.dcsa.ebl.service.BookingTOService;
import org.springframework.http.HttpStatus;
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
public class BookingController extends ExtendedBaseController<BookingTOService, BookingTO, String> {

    private final BookingTOService bookingTOService;

    @Override
    public BookingTOService getService() {
        return bookingTOService;
    }

    @Override
    public String getType() {
        return "Booking";
    }

    @GetMapping
    @Override
    public Flux<BookingTO> findAll(ServerHttpResponse response, ServerHttpRequest request) {
        return super.findAll(response, request);
    }

    @GetMapping( path = "{bookingReference}" )
    @Override
    public Mono<BookingTO> findById(@PathVariable String bookingReference) {
        return super.findById(bookingReference);
    }

    @PostMapping
    @Override
    public Mono<BookingTO> create(@Valid @RequestBody BookingTO booking) {
        return Mono.error(new CreateException("Not possible to create a Booking"));
    }

    @PutMapping( path = "{bookingReference}" )
    @Override
    public Mono<BookingTO> update(@PathVariable String bookingReference, @Valid @RequestBody BookingTO booking) {
        return Mono.error(new UpdateException("Not possible to update a Booking"));
    }

    @DeleteMapping
    @ResponseStatus( HttpStatus.NO_CONTENT )
    @Override
    public Mono<Void> delete(@RequestBody BookingTO bookingTO) {
        return Mono.error(new DeleteException("Not possible to delete a Booking"));
    }

    @DeleteMapping( path = "{bookingReference}" )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Override
    public Mono<Void> deleteById(@PathVariable String bookingReference) {
        return Mono.error(new DeleteException("Not possible to delete a Booking"));
    }
}
