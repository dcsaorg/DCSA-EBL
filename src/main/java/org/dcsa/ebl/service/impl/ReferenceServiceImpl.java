package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.dcsa.ebl.model.Reference;
import org.dcsa.ebl.repository.ReferenceRepository;
import org.dcsa.ebl.service.ReferenceService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ReferenceServiceImpl extends ExtendedBaseServiceImpl<ReferenceRepository, Reference, UUID> implements ReferenceService {
    private final ReferenceRepository referenceRepository;

    @Override
    public ReferenceRepository getRepository() {
        return referenceRepository;
    }

    public Flux<Reference> findAllByShippingInstructionID(UUID shippingInstructionID) {
        return referenceRepository.findAllByShippingInstructionID(shippingInstructionID);
    }
}
