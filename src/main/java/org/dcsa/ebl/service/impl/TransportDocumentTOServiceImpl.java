package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.exception.CreateException;
import org.dcsa.core.exception.GetException;
import org.dcsa.core.extendedrequest.ExtendedRequest;
import org.dcsa.ebl.model.*;
import org.dcsa.ebl.model.base.AbstractCharge;
import org.dcsa.ebl.model.base.AbstractTransportDocument;
import org.dcsa.ebl.model.transferobjects.CargoItemTO;
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
                    .flatMap(
                            transportDocument -> {
                                MappingUtil.copyFields(
                                        transportDocument,
                                        transportDocumentTO,
                                        AbstractTransportDocument.class
                                );
                                if (transportDocument.getShippingInstructionID() == null) {
                                    return Mono.error(new GetException("No ShippingInstruction connected to this TransportDocument - internal error!"));
                                } else {
                                    return Flux.concat(
                                            shippingInstructionTOService.findById(transportDocument.getShippingInstructionID())
                                                    .flatMap(
                                                            shippingInstruction -> {
                                                                transportDocumentTO.setShippingInstruction(shippingInstruction);
                                                                String carrierBookingReference = null;
                                                                if (shippingInstruction.getCarrierBookingReference() != null) {
                                                                    // Use the carrierBookingReference on the ShippingInstruction
                                                                    carrierBookingReference = shippingInstruction.getCarrierBookingReference();
                                                                } else {
                                                                    List<CargoItemTO> cargoItems = shippingInstruction.getCargoItems();
                                                                    if (cargoItems != null) {
                                                                        for (CargoItemTO cargoItem : cargoItems) {
                                                                            if (cargoItem.getCarrierBookingReference() != null) {
                                                                                // Assume https://github.com/dcsaorg/DCSA-EBL/issues/56 is valid
                                                                                carrierBookingReference = cargoItem.getCarrierBookingReference();
                                                                                break;
                                                                            }
                                                                        }
                                                                    }
                                                                }

                                                                if (carrierBookingReference == null) {
                                                                    return Mono.error(new GetException("No CarrierBookingReference specified on ShippingInstruction:" + shippingInstruction.getId() + " - internal error!"));
                                                                } else {
                                                                    return bookingService.findById(carrierBookingReference)
                                                                            .flatMap(booking -> {
                                                                                transportDocumentTO.setServiceTypeAtOrigin(booking.getServiceTypeAtOrigin());
                                                                                transportDocumentTO.setServiceTypeAtDestination(booking.getServiceTypeAtDestination());
                                                                                transportDocumentTO.setShipmentTermAtOrigin(booking.getShipmentTermAtOrigin());
                                                                                transportDocumentTO.setShipmentTermAtDestination(booking.getShipmentTermAtDestination());
                                                                                transportDocumentTO.setServiceContract(booking.getServiceContract());
                                                                                return Mono.just(shippingInstruction);
                                                                            })
                                                                }
                                                            }
                                                    )
                                                    .then(Mono.just(transportDocumentTO)),
                                            locationService.findById(transportDocument.getPlaceOfIssue())
                                                    .flatMap(
                                                            location -> {
                                                                transportDocumentTO.setPlaceOfIssueLocation(location);
                                                                return Mono.just(location);
                                                            }
                                                    )
                                    ).then(Mono.just(transportDocumentTO));
                                }
                    }),
                includeCharges ?
                    chargeService.findAllByTransportDocumentID(transportDocumentID)
                            .flatMap(charge -> locationService.findById(charge.getFreightPayableAt())
                                    .map(location -> {
                                        ChargeTO chargeTO = MappingUtil.instanceFrom(charge, ChargeTO::new, AbstractCharge.class);
                                        chargeTO.setFreightPayableAtLocation(location);
                                        return chargeTO;
                                    }))
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
