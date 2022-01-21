package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.events.model.Booking;
import org.dcsa.core.events.repository.BookingRepository;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.dcsa.ebl.service.BookingService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class BookingServiceImpl extends ExtendedBaseServiceImpl<BookingRepository, Booking, UUID> implements BookingService {
    private final BookingRepository bookingRepository;

    @Override
    public BookingRepository getRepository() {
        return bookingRepository;
    }
}
