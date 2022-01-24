package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.events.model.PartyContactDetails;
import org.dcsa.core.events.repository.PartyContactDetailsRepository;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.dcsa.ebl.service.PartyContactDetailsService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class PartyContactDetailsServiceImpl extends ExtendedBaseServiceImpl<PartyContactDetailsRepository, PartyContactDetails, UUID> implements PartyContactDetailsService {
    private final PartyContactDetailsRepository partyContactDetailsRepository;

    @Override
    public PartyContactDetailsRepository getRepository() {
        return partyContactDetailsRepository;
    }
}
