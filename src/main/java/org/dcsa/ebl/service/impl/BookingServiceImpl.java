package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.dcsa.ebl.model.Booking;
import org.dcsa.ebl.repository.BookingRepository;
import org.dcsa.ebl.service.BookingService;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class BookingServiceImpl extends ExtendedBaseServiceImpl<BookingRepository, Booking, String> implements BookingService {
    private final BookingRepository bookingRepository;

    @Override
    public BookingRepository getRepository() {
        return bookingRepository;
    }
}
