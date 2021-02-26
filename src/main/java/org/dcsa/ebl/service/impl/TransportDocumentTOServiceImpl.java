package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.exception.CreateException;
import org.dcsa.core.extendedrequest.ExtendedRequest;
import org.dcsa.ebl.Util;
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
    private final ShipmentService shipmentService;
    private final ShipmentTransportService shipmentTransportService;

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
                                        .doOnNext(transportDocumentTO::setShippingInstruction)
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
            // In case a new TransportDocument is created it is intentional to send an empty
            // array if no charges are inlcuded...
            transportDocumentTO.setCharges(Collections.emptyList());
            return Flux.empty();
        } else {
            return Flux.fromIterable(chargeTOs)
                    .map(chargeTO -> {
                        // Insert TransportDocumentID on all Charges
                        chargeTO.setTransportDocumentID(transportDocumentTO.getId());
                        // Create a Charge object for all ChargeTOs
                        return MappingUtil.instanceFrom(
                                chargeTO,
                                Charge::new,
                                AbstractCharge.class
                        );
                    })
                    .buffer(Util.SQL_LIST_BUFFER_SIZE)
                    .concatMap(chargeService::createAll);
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
                                    return Mono.error(new IllegalStateException("No ShippingInstruction connected to this TransportDocument - internal error!"));
                                } else {
                                    return Flux.concat(
                                            shippingInstructionTOService.findById(transportDocument.getShippingInstructionID())
                                                    .flatMap(
                                                            shippingInstructionTO -> {
                                                                transportDocumentTO.setShippingInstruction(shippingInstructionTO);
                                                                String carrierBookingReference = shippingInstructionTOService.getCarrierBookingReference(shippingInstructionTO);
                                                                if (carrierBookingReference == null) {
                                                                    return Mono.error(new IllegalStateException("No CarrierBookingReference specified on ShippingInstruction:" + shippingInstructionTO.getId() + " - internal error!"));
                                                                } else {
//                                                                    return Flux.concat(
                                                                            return bookingService.findById(carrierBookingReference)
                                                                                    .flatMap(booking -> {
                                                                                        transportDocumentTO.setServiceTypeAtOrigin(booking.getServiceTypeAtOrigin());
                                                                                        transportDocumentTO.setServiceTypeAtDestination(booking.getServiceTypeAtDestination());
                                                                                        transportDocumentTO.setShipmentTermAtOrigin(booking.getShipmentTermAtOrigin());
                                                                                        transportDocumentTO.setShipmentTermAtDestination(booking.getShipmentTermAtDestination());
                                                                                        transportDocumentTO.setServiceContract(booking.getServiceContract());
                                                                                        return Mono.just(booking);
                                                                                    })
//                                                                            ,shipmentService.findByCarrierBookingReference(carrierBookingReference)
//                                                                                    .next()
//                                                                                    .flatMap(shipment -> {
//                                                                                        shipmentTransportService.findByShipmentIDOrderBySequenceNumber(shipment.getId())
//                                                                                                .next()
//                                                                                                .flatMap(shipmentTransport -> xxx)
//                                                                                    })
//                                                                    )
//                                                                    .count()
                                                                    .then();
                                                                }
                                                            }
                                                    )
                                                    .then(),
                                            locationService.findById(transportDocument.getPlaceOfIssue())
                                                    .doOnNext(transportDocumentTO::setPlaceOfIssueLocation)
                                    ).then();
                                }
                    }),
                includeCharges ?
                    chargeService.findAllByTransportDocumentID(transportDocumentID)
                            .map(charge -> MappingUtil.instanceFrom(charge, ChargeTO::new, AbstractCharge.class))
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
