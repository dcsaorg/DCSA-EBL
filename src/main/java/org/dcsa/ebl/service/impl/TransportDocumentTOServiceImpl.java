package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.exception.CreateException;
import org.dcsa.core.extendedrequest.ExtendedRequest;
import org.dcsa.ebl.Util;
import org.dcsa.ebl.model.Booking;
import org.dcsa.ebl.model.Charge;
import org.dcsa.ebl.model.Clause;
import org.dcsa.ebl.model.TransportDocument;
import org.dcsa.ebl.model.base.AbstractCharge;
import org.dcsa.ebl.model.base.AbstractClause;
import org.dcsa.ebl.model.base.AbstractTransportDocument;
import org.dcsa.ebl.model.transferobjects.ChargeTO;
import org.dcsa.ebl.model.transferobjects.ClauseTO;
import org.dcsa.ebl.model.transferobjects.TransportDocumentTO;
import org.dcsa.ebl.model.utils.MappingUtil;
import org.dcsa.ebl.repository.BookingRepository;
import org.dcsa.ebl.repository.LocationRepository;
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
    private final LocationRepository locationRepository;
    private final BookingRepository bookingRepository;
    private final ShipmentService shipmentService;
    private final ShipmentTransportService shipmentTransportService;

    @Transactional
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
                                        .flatMap(shippingInstructionTO -> {
                                            transportDocumentTO.setShippingInstruction(shippingInstructionTO);
                                            String carrierBookingReference = shippingInstructionTOService.getCarrierBookingReference(shippingInstructionTO);
                                            if (carrierBookingReference == null) {
                                                return Mono.error(new IllegalStateException("No CarrierBookingReference specified on ShippingInstruction:" + shippingInstructionTO.getId() + " - internal error!"));
                                            } else {
                                                return Flux.concat(
                                                        updateTransportDocumentWithBookingInfo(carrierBookingReference, transportDocumentTO),
                                                        createCharges(transportDocumentTO, shippingInstructionTO.getIsChargesDisplayed()),
                                                        updateTransportDocumentWithTransportPlan(transportDocumentTO)
                                                ).then();
                                            }
                                        })
                                        .thenReturn(transportDocumentTO),
                                // Create charges if any
                                // Create/Update clauses if any
                                createClauses(transportDocumentTO)
                        )
                        .then(Mono.just(transportDocumentTO));
                    });
        }
    }

    /**
     * Checks if TransportDocument differs from Booking with regards to ServiceType, ShipmentTerms and ContractService.
     * Sets the above mentioned values from Booking on TransportDocument
     * @param carrierBookingReference the reference to the Booking
     * @param transportDocumentTO the TransportDocument to update
     * @return Error if a discrepancy is found otherwise returns the Booking
     */
    private Mono<Booking> updateTransportDocumentWithBookingInfo(String carrierBookingReference, TransportDocumentTO transportDocumentTO) {
        // Check if TransportDocument differs from values in Booking
        return getBooking(carrierBookingReference, transportDocumentTO.getShippingInstructionID())
                .flatMap(booking -> {
                    if (transportDocumentTO.getServiceTypeAtOrigin() != null && !transportDocumentTO.getServiceTypeAtOrigin().equals(booking.getServiceTypeAtOrigin())) {
                        return Mono.error(new CreateException("It is not possible to change ServiceTypeAtOrigin when creating a new TransportDocument. Please change this via booking"));
                    } else {
                        transportDocumentTO.setServiceTypeAtOrigin(booking.getServiceTypeAtOrigin());
                    }
                    if (transportDocumentTO.getServiceTypeAtDestination() != null && !transportDocumentTO.getServiceTypeAtDestination().equals(booking.getServiceTypeAtDestination())) {
                        return Mono.error(new CreateException("It is not possible to change ServiceTypeAtDestination when creating a new TransportDocument. Please change this via booking"));
                    } else {
                        transportDocumentTO.setServiceTypeAtDestination(booking.getServiceTypeAtDestination());
                    }
                    if (transportDocumentTO.getShipmentTermAtOrigin() != null && !transportDocumentTO.getShipmentTermAtOrigin().equals(booking.getShipmentTermAtOrigin())) {
                        return Mono.error(new CreateException("It is not possible to change ShipmentTermAtOrigin when creating a new TransportDocument. Please change this via booking"));
                    } else {
                        transportDocumentTO.setShipmentTermAtOrigin(booking.getShipmentTermAtOrigin());
                    }
                    if (transportDocumentTO.getShipmentTermAtDestination() != null && !transportDocumentTO.getShipmentTermAtDestination().equals(booking.getShipmentTermAtDestination())) {
                        return Mono.error(new CreateException("It is not possible to change ShipmentTermAtDestination when creating a new TransportDocument. Please change this via booking"));
                    } else {
                        transportDocumentTO.setShipmentTermAtDestination(booking.getShipmentTermAtDestination());
                    }
                    if (transportDocumentTO.getServiceContract() != null && !transportDocumentTO.getServiceContract().equals(booking.getServiceContract())) {
                        return Mono.error(new CreateException("It is not possible to change ServiceContract when creating a new TransportDocument. Please change this via booking"));
                    } else {
                        transportDocumentTO.setServiceContract(booking.getServiceContract());
                    }
                    return Mono.just(booking);
                });
    }

    private Flux<Charge> createCharges(TransportDocumentTO transportDocumentTO, boolean isChargesDisplayed) {
        List<ChargeTO> chargeTOs = transportDocumentTO.getCharges();
        if (chargeTOs == null || chargeTOs.isEmpty()) {
            // In case a new TransportDocument is created it is intentional to send an empty
            // array if no charges are included...
            transportDocumentTO.setCharges(Collections.emptyList());
            return Flux.empty();
        } else {
            if (!isChargesDisplayed) {
                return Flux.error(new CreateException("isDisplayCharges is set to false on ShippingInstruction - it is not possible to create new Charges as they will not be part of the TransportDocument"));
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
    }

    private Flux<Void> createClauses(TransportDocumentTO transportDocumentTO) {
        List<ClauseTO> clauseTOs = transportDocumentTO.getClauses();
        if (clauseTOs == null || clauseTOs.isEmpty()) {
            transportDocumentTO.setClauses(Collections.emptyList());
            return Flux.empty();
        } else {
            // Save all Clauses in one Bulk
            return Flux.fromIterable(clauseTOs)
                    .map(clauseTO -> MappingUtil.instanceFrom(clauseTO, Clause::new, AbstractClause.class))
                    .buffer(Util.SQL_LIST_BUFFER_SIZE)
                    .concatMap(clauses -> clauseService.createAll(clauses)
                            // Make sure many-many relations are created
                            .concatMap(clause -> clauseService.createTransportDocumentClauseRelation(clause.getId(), transportDocumentTO.getId()))
                            .then()
                    );
        }
    }

    @Transactional
    @Override
    public Mono<TransportDocumentTO> findById(UUID transportDocumentID) {
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
                                                                    return Flux.concat(
                                                                            updateTransportDocumentWithBookingInfo(carrierBookingReference, transportDocumentTO),
                                                                            updateTransportDocumentWithCharges(transportDocumentTO, shippingInstructionTO.getIsChargesDisplayed()),
                                                                            updateTransportDocumentWithTransportPlan(transportDocumentTO)
                                                                    ).then();
                                                                }
                                                            }
                                                    )
                                                    .then(),
                                            transportDocument.getPlaceOfIssue() != null ?
                                                    locationService.findById(transportDocument.getPlaceOfIssue())
                                                    .doOnNext(transportDocumentTO::setPlaceOfIssueLocation)
                                                    : Mono.empty()
                                    ).then();
                                }
                    }),
                clauseService.findAllByTransportDocumentID(transportDocumentID)
                        .map(clause -> MappingUtil.instanceFrom(clause, ClauseTO::new, AbstractClause.class))
                        .collectList()
                        .doOnNext(transportDocumentTO::setClauses)
        ).then(Mono.just(transportDocumentTO));
    }

    private Flux<ChargeTO> updateTransportDocumentWithCharges(TransportDocumentTO transportDocumentTO, boolean isChargesDisplayed) {
        if (isChargesDisplayed) {
            return chargeService.findAllByTransportDocumentID(transportDocumentTO.getId())
                    .map(charge -> MappingUtil.instanceFrom(charge, ChargeTO::new, AbstractCharge.class))
                    .collectList()
                    .doOnNext(transportDocumentTO::setCharges)
                    .thenMany(Flux::just);
        } else {
            return Flux.empty();
        }
    }

    private Mono<Void> updateTransportDocumentWithTransportPlan(TransportDocumentTO transportDocumentTO) {
//                                                                    return Flux.concat(
//                                                                            ,shipmentService.findByCarrierBookingReference(carrierBookingReference)
//                                                                                    .next()
//                                                                                    .flatMap(shipment -> {
//                                                                                        shipmentTransportService.findByShipmentIDOrderBySequenceNumber(shipment.getId())
//                                                                                                .next()
//                                                                                                .flatMap(shipmentTransport -> xxx)
//                                                                                    })
//                                                                    )
//                                                                    .count()
//                                                                    .then();
    }

    @Override
    public Flux<TransportDocument> findAllExtended(final ExtendedRequest<TransportDocument> extendedRequest) {
        return transportDocumentService.findAllExtended(extendedRequest);
    }

    private Mono<Booking> getBooking(String carrierBookingReference, UUID shippingSInstructionID) {
        // Don't use ServiceClass - use Repository directly in order to throw internal error if BookingReference does not exist.
        return bookingRepository.findById(carrierBookingReference)
                .switchIfEmpty(Mono.error(
                        new IllegalStateException("The CarrierBookingReference: " + carrierBookingReference + " specified on ShippingInstruction:" + shippingSInstructionID.toString() + " does not exist!")
                ));
    }
}
