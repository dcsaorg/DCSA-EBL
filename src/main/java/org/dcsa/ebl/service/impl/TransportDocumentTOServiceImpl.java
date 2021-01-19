package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.exception.CreateException;
import org.dcsa.core.exception.GetException;
import org.dcsa.core.extendedrequest.ExtendedRequest;
import org.dcsa.ebl.model.Charge;
import org.dcsa.ebl.model.TransportDocument;
import org.dcsa.ebl.model.base.AbstractTransportDocument;
import org.dcsa.ebl.model.transferobjects.TransportDocumentTO;
import org.dcsa.ebl.model.utils.MappingUtil;
import org.dcsa.ebl.service.ChargeService;
import org.dcsa.ebl.service.ShippingInstructionTOService;
import org.dcsa.ebl.service.TransportDocumentService;
import org.dcsa.ebl.service.TransportDocumentTOService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class TransportDocumentTOServiceImpl implements TransportDocumentTOService {
    private final TransportDocumentService transportDocumentService;
    private final ShippingInstructionTOService shippingInstructionTOService;
    private final ChargeService chargeService;

    @Override
    public Mono<TransportDocumentTO> create(TransportDocumentTO transportDocumentTO) {
        TransportDocument transportDocument = MappingUtil.instanceFrom(
                transportDocumentTO,
                TransportDocument::new,
                AbstractTransportDocument.class
        );
        if (transportDocumentTO.getShippingInstruction() != null) {
            return Mono.error(new CreateException("ShippingInstruction object cannot be included when creating a TransportDocument"));
        } else {
            return transportDocumentService.create(transportDocument)
                    .flatMap(td -> {
                        transportDocumentTO.setId(td.getId());
                        return Flux.concat(
                                shippingInstructionTOService.findById(transportDocument.getShippingInstructionID())
                                        .switchIfEmpty(
                                                Mono.error(new GetException("ShippingInstruction linked to from TransportDocument does not exist"))
                                        )
                                        .doOnNext(shippingInstruction ->
                                                transportDocumentTO.setShippingInstruction(shippingInstruction)
                                        )
                                        .thenReturn(transportDocumentTO),
                                createCharges(transportDocumentTO)
                        )
                        .then(Mono.just(transportDocumentTO));
                    });
        }
    }

    private Flux<Charge> createCharges(TransportDocumentTO transportDocumentTO) {
        UUID transportDocumentID = transportDocumentTO.getId();
        List<Charge> charges = transportDocumentTO.getCharges();
        if (charges == null || charges.isEmpty()) {
            transportDocumentTO.setCharges(Collections.emptyList());
            return Flux.empty();
        } else {
            // Insert TransportDocumentID on all Charges
            charges.stream().forEach(charge -> charge.setTransportDocumentID(transportDocumentID));
            // Save all Charges in one bulk
            return chargeService.createAll(charges);
        }
    }

    @Transactional
    @Override
    public Mono<TransportDocumentTO> findById(UUID transportDocumentID, boolean includeCharges) {
        TransportDocumentTO transportDocumentTO = new TransportDocumentTO();

        return Flux.concat(
                transportDocumentService.findById(transportDocumentID)
                    .switchIfEmpty(
                            Mono.error(new GetException("No TransportDocument for id: " + transportDocumentID))
                    )
                    .flatMap(
                            transportDocument -> {
                                MappingUtil.copyFields(
                                        transportDocument,
                                        transportDocumentTO,
                                        AbstractTransportDocument.class
                                );
                                if (transportDocument.getShippingInstructionID() == null) {
                                    return Mono.error(new GetException("No ShippingInstruction connected to this TransportDocument"));
                                } else {
                                    return shippingInstructionTOService.findById(transportDocument.getShippingInstructionID())
                                            .switchIfEmpty(
                                                    Mono.error(new GetException("ShippingInstruction linked tp from TransportDocument does not exist"))
                                            )
                                            .doOnNext(
                                                    shippingInstruction ->
                                                            transportDocumentTO.setShippingInstruction(shippingInstruction))
                                            .thenReturn(transportDocumentTO);
                                }
                    }),
                includeCharges ?
                    chargeService.findAllByTransportDocumentID(transportDocumentID)
                            .collectList()
                            .doOnNext(transportDocumentTO::setCharges) : Flux.empty()
        ).then(Mono.just(transportDocumentTO));
    }

    @Override
    public Flux<TransportDocument> findAllExtended(final ExtendedRequest<TransportDocument> extendedRequest) {
        return transportDocumentService.findAllExtended(extendedRequest);
    }
}
