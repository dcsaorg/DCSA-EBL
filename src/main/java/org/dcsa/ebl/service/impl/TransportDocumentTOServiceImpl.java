package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.exception.CreateException;
import org.dcsa.core.exception.GetException;
import org.dcsa.core.extendedrequest.ExtendedRequest;
import org.dcsa.ebl.model.Charge;
import org.dcsa.ebl.model.Clause;
import org.dcsa.ebl.model.TransportDocument;
import org.dcsa.ebl.model.TransportPlan;
import org.dcsa.ebl.model.base.AbstractCharge;
import org.dcsa.ebl.model.base.AbstractTransportDocument;
import org.dcsa.ebl.model.transferobjects.ChargeTO;
import org.dcsa.ebl.model.transferobjects.TransportDocumentTO;
import org.dcsa.ebl.model.utils.MappingUtil;
import org.dcsa.ebl.service.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class TransportDocumentTOServiceImpl implements TransportDocumentTOService {
    private final TransportDocumentService transportDocumentService;
    private final ShippingInstructionTOService shippingInstructionTOService;
    private final ChargeService chargeService;
    private final ClauseService clauseService;
    private final LocationService locationService;
    private final BookingService bookingService;

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
                                createCharges(transportDocumentTO),
                                createClauses(transportDocumentTO),
                                createTransportPlan(transportDocumentTO)
                        )
                        .then(Mono.just(transportDocumentTO));
                    });
        }
    }

    private Flux<Charge> createCharges(TransportDocumentTO transportDocumentTO) {
        List<ChargeTO> chargeTOs = transportDocumentTO.getCharges();
        if (chargeTOs == null || chargeTOs.isEmpty()) {
            transportDocumentTO.setCharges(Collections.emptyList());
            return Flux.empty();
        } else {
            List<Charge> charges = new ArrayList<>(chargeTOs.size());
            chargeTOs.stream().forEach(chargeTO -> {
                // Insert TransportDocumentID on all Charges
                chargeTO.setTransportDocumentID(transportDocumentTO.getId());
                // Create a Charge object for all ChargeTOs
                Charge charge = MappingUtil.instanceFrom(
                        chargeTO,
                        Charge::new,
                        AbstractCharge.class
                );
                charges.add(charge);
            });

            // Save all Charges in one bulk
            return chargeService.createAll(charges);
        }
    }

    private Flux<Clause> createClauses(TransportDocumentTO transportDocumentTO) {
        List<Clause> clauses = transportDocumentTO.getClauses();
        if (clauses == null || clauses.isEmpty()) {
            transportDocumentTO.setClauses(Collections.emptyList());
            return Flux.empty();
        } else {
            // Save all Clauses in one Bulk
            return clauseService.createAll(clauses)
                    .concatMap(clause ->
                            // Make sure many-many relations are created
                            clauseService.createTransportDocumentClauseRelation(clause.getId(), transportDocumentTO.getId())
                                    .thenReturn(clause)
                    );
        }
    }

    private Mono<TransportPlan> createTransportPlan(TransportDocumentTO transportDocumentTO) {
        // TODO: ...
        return Mono.just(new TransportPlan());
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
                                    return Flux.concat(
                                            shippingInstructionTOService.findById(transportDocument.getShippingInstructionID())
                                                    .switchIfEmpty(
                                                            Mono.error(new GetException("ShippingInstruction linked tp from TransportDocument does not exist"))
                                                    )
                                                    .doOnNext(
                                                            shippingInstruction -> {
                                                                transportDocumentTO.setShippingInstruction(shippingInstruction);
                                                                // TODO: Find correct BookingReference...
                                                                bookingService.findById(shippingInstruction.getCargoItems().get(0).getCarrierBookingReference())
                                                                        .doOnNext(booking -> {
                                                                            transportDocumentTO.setServiceTypeAtOrigin(booking.getServiceTypeAtOrigin());
                                                                            transportDocumentTO.setServiceTypeAtDestination(booking.getServiceTypeAtDestination());
                                                                            transportDocumentTO.setShipmentTermAtOrigin(booking.getShipmentTermAtOrigin());
                                                                            transportDocumentTO.setShipmentTermAtDestination(booking.getShipmentTermAtDestination());
                                                                            transportDocumentTO.setServiceContract(booking.getServiceContract());
                                                                        });
                                                            }
                                                    ),
//                                                    .thenReturn(transportDocumentTO),
                                            locationService.findById(transportDocument.getPlaceOfIssue())
                                                    .doOnNext(
                                                            location ->
                                                                    transportDocumentTO.setPlaceOfIssueLocation(location)
                                                    )
                                    ).then(Mono.just(transportDocumentTO));
                                }
                    }),
                includeCharges ?
                    chargeService.findAllByTransportDocumentID(transportDocumentID)
                            .flatMap(charge -> {
                                return locationService.findById(charge.getFreightPayableAt())
                                        .map(location -> {
                                            ChargeTO chargeTO = MappingUtil.instanceFrom(charge, ChargeTO::new, AbstractCharge.class);
                                            chargeTO.setFreightPayableAtLocation(location);
                                            return chargeTO;
                                        });
                            })
                            .collectList()
                            .doOnNext(transportDocumentTO::setCharges)
                        : Flux.empty(),
                clauseService.findAllByTransportDocumentID(transportDocumentID)
                            .collectList()
                            .doOnNext(transportDocumentTO::setClauses)
        ).then(Mono.just(transportDocumentTO));
    }

    @Override
    public Flux<TransportDocument> findAllExtended(final ExtendedRequest<TransportDocument> extendedRequest) {
        return transportDocumentService.findAllExtended(extendedRequest);
    }
}
