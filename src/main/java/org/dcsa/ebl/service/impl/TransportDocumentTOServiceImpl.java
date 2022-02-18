package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.events.model.Booking;
import org.dcsa.core.events.model.Charge;
import org.dcsa.core.events.model.TransportDocument;
import org.dcsa.core.events.repository.BookingRepository;
import org.dcsa.core.exception.CreateException;
import org.dcsa.core.extendedrequest.ExtendedRequest;
import org.dcsa.ebl.model.transferobjects.ChargeTO;
import org.dcsa.ebl.model.transferobjects.TransportDocumentTO;
import org.dcsa.ebl.service.TransportDocumentService;
import org.dcsa.ebl.service.TransportDocumentTOService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Service
public class TransportDocumentTOServiceImpl implements TransportDocumentTOService {
  private final TransportDocumentService transportDocumentService;
  private final BookingRepository bookingRepository;

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
          return Flux.empty();
      }
    }
  }

  @Transactional
  @Override
  public Mono<TransportDocumentTO> findById(String transportDocumentReference) {
//    TransportDocumentTO transportDocumentTO = new TransportDocumentTO();
//
//    return Flux.concat(
//            transportDocumentService
//                .findById(transportDocumentReference)
//                .flatMapMany(
//                    transportDocument -> {
//                      if (transportDocument.getShippingInstructionID() == null) {
//                        return Mono.error(
//                            new IllegalStateException(
//                                "No ShippingInstruction connected to this TransportDocument - internal error!"));
//                      } else {
//                        return Flux.concat(
//                            shippingInstructionTOService
//                                .findById(transportDocument.getShippingInstructionID())
//                                .flatMapMany(
//                                    shippingInstructionTO -> {
//                                      transportDocumentTO.setShippingInstruction(
//                                          shippingInstructionTO);
//                                      String carrierBookingReference =
//                                          shippingInstructionTOService.getCarrierBookingReference(
//                                              shippingInstructionTO);
//                                      if (carrierBookingReference == null) {
//                                        return Flux.error(
//                                            new IllegalStateException(
//                                                "No CarrierBookingReference specified on ShippingInstruction:"
//                                                    + shippingInstructionTO
//                                                        .getShippingInstructionID()
//                                                    + " - internal error!"));
//                                      } else {
//                                        return Flux.concat(
//                                            updateTransportDocumentWithBookingInfo(
//                                                carrierBookingReference, transportDocumentTO),
//                                            updateTransportDocumentWithTransportPlan(
//                                                carrierBookingReference, transportDocumentTO));
//                                      }
//                                    }),
//                            transportDocument.getPlaceOfIssue() != null
//                                ? locationService
//                                    .findById(transportDocument.getPlaceOfIssue())
//                                    .doOnNext(transportDocumentTO::setPlaceOfIssueLocation)
//                                : Mono.empty());
//                      }
//                    }),
//            clauseService
//                .findAllByTransportDocumentReference(transportDocumentReference)
//                .map(
//                    clause -> MappingUtil.instanceFrom(clause, ClauseTO::new, AbstractClause.class))
//                .collectList()
//                .doOnNext(transportDocumentTO::setClauses))
//        .then(Mono.just(transportDocumentTO));
    return Mono.empty();
  }

  private Mono<Void> updateTransportDocumentWithCharges(TransportDocumentTO transportDocumentTO, boolean isChargesDisplayed) {
      return Mono.empty();
  }

  private Mono<Void> updateTransportDocumentWithTransportPlan(
      String carrierBookingReference, TransportDocumentTO transportDocumentTO) {
      return Mono.empty();
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
