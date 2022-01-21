package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.events.model.Booking;
import org.dcsa.core.events.model.Charge;
import org.dcsa.core.events.model.TransportDocument;
import org.dcsa.core.events.repository.BookingRepository;
import org.dcsa.core.events.service.LocationService;
import org.dcsa.core.events.service.VoyageService;
import org.dcsa.core.exception.CreateException;
import org.dcsa.core.extendedrequest.ExtendedRequest;
import org.dcsa.ebl.Util;
import org.dcsa.ebl.model.Clause;
import org.dcsa.ebl.model.TransportPlan;
import org.dcsa.ebl.model.base.AbstractCharge;
import org.dcsa.ebl.model.base.AbstractClause;
import org.dcsa.ebl.model.base.AbstractShipmentLocation;
import org.dcsa.ebl.model.base.AbstractTransportDocument;
import org.dcsa.ebl.model.transferobjects.*;
import org.dcsa.ebl.model.utils.MappingUtil;
import org.dcsa.ebl.service.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class TransportDocumentTOServiceImpl implements TransportDocumentTOService {
  private final TransportDocumentService transportDocumentService;
  private final ShippingInstructionTOService shippingInstructionTOService;
  private final ChargeService chargeService;
  private final ClauseService clauseService;
  private final LocationService locationService;
  private final BookingRepository bookingRepository;
  private final ShipmentLocationService shipmentLocationService;
  private final ExtendedShipmentTransportService extendedShipmentTransportService;
  private final ShipmentService shipmentService;
  private final VoyageService voyageService;

  @Transactional
  @Override
  public Mono<TransportDocumentTO> create(TransportDocumentTO transportDocumentTO) {
      return Mono.empty();
      // TODO: fix me
//    TransportDocument transportDocument = MappingUtil.instanceFrom(transportDocumentTO, TransportDocument::new, AbstractTransportDocument.class);
//    if (transportDocumentTO.getShippingInstruction() != null) {
//      return Mono.error(
//          new CreateException(
//              "ShippingInstruction object cannot be included when creating a TransportDocument"));
//    } else {
//      return transportDocumentService
//          .create(transportDocument)
//          .flatMap(
//              td -> {
//                transportDocumentTO.setTransportDocumentReference(
//                    td.getTransportDocumentReference());
//                return Flux.concat(
//                        shippingInstructionTOService
//                            .findById(transportDocument.getShippingInstructionID())
//                            .flatMap(
//                                shippingInstructionTO -> {
//                                  transportDocumentTO.setShippingInstruction(shippingInstructionTO);
//                                  String carrierBookingReference =
//                                      shippingInstructionTOService.getCarrierBookingReference(
//                                          shippingInstructionTO);
//                                  if (carrierBookingReference == null) {
//                                    return Mono.error(
//                                        new IllegalStateException(
//                                            "No CarrierBookingReference specified on ShippingInstruction:"
//                                                + shippingInstructionTO.getShippingInstructionID()
//                                                + " - internal error!"));
//                                  } else {
//                                    return Flux.concat(
//                                            updateTransportDocumentWithBookingInfo(
//                                                carrierBookingReference, transportDocumentTO),
//                                            createCharges(
//                                                transportDocumentTO,
//                                                shippingInstructionTO.getIsChargesDisplayed()),
//                                            updateTransportDocumentWithTransportPlan(
//                                                carrierBookingReference, transportDocumentTO))
//                                        .then();
//                                  }
//                                })
//                            .thenReturn(transportDocumentTO),
//                        // Create charges if any
//                        // Create/Update clauses if any
//                        createClauses(transportDocumentTO))
//                    .then(Mono.just(transportDocumentTO));
//              });
//    }
  }

  /**
   * Checks if TransportDocument differs from Booking with regards to ServiceType, ShipmentTerms and
   * ContractService. Sets the above mentioned values from Booking on TransportDocument
   *
   * @param carrierBookingReference the reference to the Booking
   * @param transportDocumentTO the TransportDocument to update
   * @return Error if a discrepancy is found otherwise returns the Booking
   */
  private Mono<Booking> updateTransportDocumentWithBookingInfo(String carrierBookingReference, TransportDocumentTO transportDocumentTO) {

      return Mono.empty();
      // TODO: fix me
    // Check if TransportDocument differs from values in Booking
//    return getBooking(carrierBookingReference, transportDocumentTO.getShippingInstructionID())
//        .flatMap(
//            booking -> {
//              if (transportDocumentTO.getServiceTypeAtOrigin() != null
//                  && !Objects.equals(
//                      transportDocumentTO.getServiceTypeAtOrigin(),
//                      booking.getServiceTypeAtOrigin())) {
//                return getBookingError(
//                    "ServiceTypeAtOrigin",
//                    Objects.toString(booking.getServiceTypeAtOrigin()),
//                    Objects.toString(transportDocumentTO.getServiceTypeAtOrigin()));
//              } else {
//                transportDocumentTO.setServiceTypeAtOrigin(booking.getServiceTypeAtOrigin());
//              }
//              if (transportDocumentTO.getServiceTypeAtDestination() != null
//                  && !Objects.equals(
//                      transportDocumentTO.getServiceTypeAtDestination(),
//                      booking.getServiceTypeAtDestination())) {
//                return getBookingError(
//                    "ServiceTypeAtDestination",
//                    Objects.toString(booking.getServiceTypeAtDestination()),
//                    Objects.toString(transportDocumentTO.getServiceTypeAtDestination()));
//              } else {
//                transportDocumentTO.setServiceTypeAtDestination(
//                    booking.getServiceTypeAtDestination());
//              }
//              if (transportDocumentTO.getShipmentTermAtOrigin() != null && !Objects.equals(transportDocumentTO.getShipmentTermAtOrigin(), booking.getShipmentTermAtOrigin())) {
//                return getBookingError(
//                    "ShipmentTermAtOrigin",
//                    Objects.toString(booking.getShipmentTermAtOrigin()),
//                    Objects.toString(transportDocumentTO.getShipmentTermAtOrigin()));
//              } else {
//                transportDocumentTO.setShipmentTermAtOrigin(booking.getShipmentTermAtOrigin());
//              }
//              if (transportDocumentTO.getShipmentTermAtDestination() != null
//                  && !Objects.equals(
//                      transportDocumentTO.getShipmentTermAtDestination(),
//                      booking.getShipmentTermAtDestination())) {
//                return getBookingError(
//                    "ShipmentTermAtDestination",
//                    Objects.toString(booking.getShipmentTermAtDestination()),
//                    Objects.toString(transportDocumentTO.getShipmentTermAtDestination()));
//              } else {
//                transportDocumentTO.setShipmentTermAtDestination(
//                    booking.getShipmentTermAtDestination());
//              }
//              if (transportDocumentTO.getServiceContract() != null
//                  && !Objects.equals(
//                      transportDocumentTO.getServiceContract(), booking.getServiceContract())) {
//                return getBookingError(
//                    "ServiceContract",
//                    booking.getServiceContract(),
//                    transportDocumentTO.getServiceContract());
//              } else {
//                transportDocumentTO.setServiceContract(booking.getServiceContract());
//              }
//              return Mono.just(booking);
//            });
  }

  private <T> Mono<T> getBookingError(String fieldName, String fromValue, String toValue) {
    return Mono.error(
        new CreateException(
            "It is not possible to change "
                + fieldName
                + " from "
                + fromValue
                + " to "
                + toValue
                + " when creating a new TransportDocument. Please change this via booking"));
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
        return Flux.error(
            new CreateException(
                "isDisplayCharges is set to false on ShippingInstruction - it is not possible to create new Charges as they will not be part of the TransportDocument"));
      } else {
          // TODO: fix me
          return Flux.empty();
//        return Flux.fromIterable(chargeTOs)
//            .map(
//                chargeTO -> {
//                  // Insert TransportDocumentReference on all Charges
//                  chargeTO.setTransportDocumentReference(
//                      transportDocumentTO.getTransportDocumentReference());
//                  // Create a Charge object for all ChargeTOs
//                  return MappingUtil.instanceFrom(chargeTO, Charge::new, AbstractCharge.class);
//                })
//            .buffer(Util.SQL_LIST_BUFFER_SIZE)
//            .concatMap(chargeService::createAll);
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
          .concatMap(
              clauses ->
                  clauseService
                      .createAll(clauses)
                      // Make sure many-many relations are created
                      .concatMap(
                          clause ->
                              clauseService.createTransportDocumentClauseRelation(
                                  clause.getId(),
                                  transportDocumentTO.getTransportDocumentReference()))
                      .then());
    }
  }

  @Transactional
  @Override
  public Mono<TransportDocumentTO> findById(String transportDocumentReference) {
    TransportDocumentTO transportDocumentTO = new TransportDocumentTO();

    return Flux.concat(
            transportDocumentService
                .findById(transportDocumentReference)
                .flatMapMany(
                    transportDocument -> {
                        // TODO: fix me
//                      MappingUtil.copyFields(transportDocument, transportDocumentTO, AbstractTransportDocument.class);
                      if (transportDocument.getShippingInstructionID() == null) {
                        return Mono.error(
                            new IllegalStateException(
                                "No ShippingInstruction connected to this TransportDocument - internal error!"));
                      } else {
                        return Flux.concat(
                            shippingInstructionTOService
                                .findById(transportDocument.getShippingInstructionID())
                                .flatMapMany(
                                    shippingInstructionTO -> {
                                      transportDocumentTO.setShippingInstruction(
                                          shippingInstructionTO);
                                      String carrierBookingReference =
                                          shippingInstructionTOService.getCarrierBookingReference(
                                              shippingInstructionTO);
                                      if (carrierBookingReference == null) {
                                        return Flux.error(
                                            new IllegalStateException(
                                                "No CarrierBookingReference specified on ShippingInstruction:"
                                                    + shippingInstructionTO
                                                        .getShippingInstructionID()
                                                    + " - internal error!"));
                                      } else {
                                        return Flux.concat(
                                            updateTransportDocumentWithBookingInfo(
                                                carrierBookingReference, transportDocumentTO),
                                            updateTransportDocumentWithCharges(
                                                transportDocumentTO,
                                                shippingInstructionTO.getIsChargesDisplayed()),
                                            updateTransportDocumentWithTransportPlan(
                                                carrierBookingReference, transportDocumentTO));
                                      }
                                    }),
                            transportDocument.getPlaceOfIssue() != null
                                ? locationService
                                    .findById(transportDocument.getPlaceOfIssue())
                                    .doOnNext(transportDocumentTO::setPlaceOfIssueLocation)
                                : Mono.empty());
                      }
                    }),
            clauseService
                .findAllByTransportDocumentReference(transportDocumentReference)
                .map(
                    clause -> MappingUtil.instanceFrom(clause, ClauseTO::new, AbstractClause.class))
                .collectList()
                .doOnNext(transportDocumentTO::setClauses))
        .then(Mono.just(transportDocumentTO));
  }

  private Mono<Void> updateTransportDocumentWithCharges(TransportDocumentTO transportDocumentTO, boolean isChargesDisplayed) {
      // TODO: fix me
//    if (isChargesDisplayed) {
//      return chargeService
//          .findAllByTransportDocumentReference(transportDocumentTO.getTransportDocumentReference())
//          .map(charge -> MappingUtil.instanceFrom(charge, ChargeTO::new, AbstractCharge.class))
//          .collectList()
//          .doOnNext(transportDocumentTO::setCharges)
//          .then();
//    } else {
      return Mono.empty();
//    }
  }

  private Mono<Void> updateTransportDocumentWithTransportPlan(
      String carrierBookingReference, TransportDocumentTO transportDocumentTO) {
      return Mono.empty();
              // TODO: fix me
//    TransportPlan transportPlan = new TransportPlan();
//    transportDocumentTO.setTransportPlan(transportPlan);
//    return Flux.concat(
//            // Update ShipmentLocations on TransportPlan
//            shipmentLocationService
//                .findAllByCarrierBookingReference(carrierBookingReference)
//                .concatMap(
//                    shipmentLocation -> {
//                      ShipmentLocationTO shipmentLocationTO = MappingUtil.instanceFrom(shipmentLocation, ShipmentLocationTO::new, AbstractShipmentLocation.class);
//                      return locationService
//                          .findTOById(shipmentLocation.getLocationID())
//                          .map(
//                              locationTO -> {
//                                shipmentLocationTO.setLocation(locationTO);
//                                return shipmentLocationTO;
//                              });
//                    })
//                .collectList()
//                .doOnNext(transportPlan::setShipmentLocations)
//
//            // Update Transports on TransportPlan
//            ,
//            shipmentService
//                .findByCarrierBookingReference(carrierBookingReference)
//                .concatMap(
//                    shipment ->
//                        extendedShipmentTransportService
//                            .findByShipmentIDOrderBySequenceNumber(shipment.getId())
//                            .flatMap(
//                                shipmentTransportExtended -> {
//                                  TransportTO transportTO = new TransportTO();
//                                  transportTO.setLoadLocation(
//                                      shipmentTransportExtended.getLoadLocationTO());
//                                  transportTO.setDischargeLocation(
//                                      shipmentTransportExtended.getDischargeLocationTO());
//                                  transportTO.setModeOfTransport(
//                                      shipmentTransportExtended.getModeOfTransport());
//                                  transportTO.setVesselIMONumber(
//                                      shipmentTransportExtended.getVesselIMONumber());
//                                  transportTO.setCarrierVoyageNumber(null);
//                                  transportTO.setUnderShippersResponsibility(
//                                      shipmentTransportExtended.getIsUnderShippersResponsibility());
//
//                                  // Find carrierVoyageNumber
//                                  return voyageService
//                                      .findFirstByTransportCallOrderByCarrierVoyageNumberDesc(
//                                          shipmentTransportExtended.getLoadTransportCallId())
//                                      .doOnNext(
//                                          voyage ->
//                                              transportTO.setCarrierVoyageNumber(
//                                                  voyage.getCarrierVoyageNumber()))
//                                      .thenReturn(transportTO);
//                                }))
//                .collectList()
//                .doOnNext(transportPlan::setTransports))
//        .then();
  }

  @Override
  public Flux<TransportDocument> findAllExtended(
      final ExtendedRequest<TransportDocument> extendedRequest) {
    return transportDocumentService.findAllExtended(extendedRequest);
  }

  private Mono<Booking> getBooking(String carrierBookingReference, String shippingInstructionID) {
    // Don't use ServiceClass - use Repository directly in order to throw internal error if
    // BookingReference does not exist.
    return bookingRepository
        .findByCarrierBookingRequestReference(carrierBookingReference)
        .switchIfEmpty(
            Mono.error(
                new IllegalStateException(
                    "The CarrierBookingReference: "
                        + carrierBookingReference
                        + " specified on ShippingInstruction:"
                        + shippingInstructionID
                        + " does not exist!")));
  }
}
