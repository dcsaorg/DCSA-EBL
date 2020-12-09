package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.dcsa.ebl.model.transferobjects.BookingTO;
import org.dcsa.ebl.repository.BookingRepository;
import org.dcsa.ebl.repository.BookingTORepository;
import org.dcsa.ebl.service.BookingService;
import org.dcsa.ebl.service.BookingTOService;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class BookingTOServiceImpl extends ExtendedBaseServiceImpl<BookingTORepository, BookingTO, String> implements BookingTOService {
    private final BookingTORepository BookingTORepository;

    @Override
    public BookingTORepository getRepository() {
        return BookingTORepository;
    }

    @Override
    public Class<BookingTO> getModelClass() {
        return BookingTO.class;
    }
}
