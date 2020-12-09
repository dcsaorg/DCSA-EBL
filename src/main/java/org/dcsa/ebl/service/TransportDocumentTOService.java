package org.dcsa.ebl.service;

import org.dcsa.core.service.ExtendedBaseService;
import org.dcsa.ebl.model.transferobjects.ChargeTO;
import org.dcsa.ebl.model.EBLEndorsementChain;
import org.dcsa.ebl.model.transferobjects.TransportDocumentTO;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

public interface TransportDocumentTOService extends ExtendedBaseService<TransportDocumentTO, UUID> {
    Flux<ChargeTO> updateCharges(UUID ID, List<ChargeTO> chargeList);

    Flux<EBLEndorsementChain> updateEBLEndorsementChain(UUID ID, List<EBLEndorsementChain> eblEndorsementChainList);
}
