package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.events.model.Booking;
import org.dcsa.core.events.model.Charge;
import org.dcsa.core.events.model.ShipmentEvent;
import org.dcsa.core.events.model.TransportDocument;
import org.dcsa.core.events.model.enums.CarrierCodeListProvider;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.core.events.repository.BookingRepository;
import org.dcsa.core.events.repository.CarrierRepository;
import org.dcsa.core.events.repository.ShipmentEventRepository;
import org.dcsa.core.events.repository.TransportDocumentRepository;
import org.dcsa.core.exception.ConcreteRequestErrorMessageException;
import org.dcsa.core.exception.CreateException;
import org.dcsa.core.extendedrequest.ExtendedRequest;
import org.dcsa.core.service.impl.AsymmetricQueryServiceImpl;
import org.dcsa.core.service.impl.QueryServiceImpl;
import org.dcsa.ebl.model.TransportDocumentSummary;
import org.dcsa.ebl.model.mappers.TransportDocumentMapper;
import org.dcsa.ebl.model.transferobjects.ChargeTO;
import org.dcsa.ebl.model.transferobjects.TransportDocumentTO;
import org.dcsa.ebl.repository.ShippingInstructionRepository;
import org.dcsa.ebl.service.ShippingInstructionService;
import org.dcsa.ebl.service.TransportDocumentService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class TransportDocumentServiceImpl
    extends AsymmetricQueryServiceImpl<
        TransportDocumentRepository, TransportDocument, TransportDocumentSummary, String>
    implements TransportDocumentService {

  private final TransportDocumentRepository transportDocumentRepository;
  private final BookingRepository bookingRepository;
  private final TransportDocumentMapper transportDocumentMapper;
  private final CarrierRepository carrierRepository;
  private final ShippingInstructionRepository shippingInstructionRepository;

  /**
   * Checks if TransportDocument differs from Booking with regard to ServiceType, ShipmentTerms and
   * ContractService. Sets the above-mentioned values from Booking on TransportDocument
   *
   * @param carrierBookingReference the reference to the Booking
   * @param transportDocumentTO the TransportDocument to update
   * @return Error if a discrepancy is found otherwise returns the Booking
   */
  private Mono<Booking> updateTransportDocumentWithBookingInfo(
      String carrierBookingReference, TransportDocumentTO transportDocumentTO) {

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

  private Flux<Charge> createCharges(
      TransportDocumentTO transportDocumentTO, boolean isChargesDisplayed) {
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
    return Mono.empty();
  }

  private Mono<Void> updateTransportDocumentWithCharges(
      TransportDocumentTO transportDocumentTO, boolean isChargesDisplayed) {
    return Mono.empty();
  }

  private Mono<Void> updateTransportDocumentWithTransportPlan(
      String carrierBookingReference, TransportDocumentTO transportDocumentTO) {
    return Mono.empty();
  }

  @Override
  protected Mono<TransportDocumentSummary> mapDM2TO(TransportDocument transportDocument) {
    TransportDocumentSummary transportDocumentSummary =
        transportDocumentMapper.transportDocumentToTransportDocumentSummary(transportDocument);

    return shippingInstructionRepository
        .findById(transportDocumentSummary.getShippingInstructionID())
        .flatMap(
            shippingInstruction -> {
              transportDocumentSummary.setDocumentStatus(shippingInstruction.getDocumentStatus());
              return shippingInstructionRepository
                  .findCarrierBookingReferenceByShippingInstructionID(shippingInstruction.getShippingInstructionID())
                .switchIfEmpty(Mono.error(ConcreteRequestErrorMessageException.notFound("No shipping instruction found with ID: " + shippingInstruction.getShippingInstructionID())))
                  .collectList()
                  .doOnNext(transportDocumentSummary::setCarrierBookingReferences)
                  .thenReturn(transportDocumentSummary);
            })
        .flatMap(
            ignored -> {
              if (transportDocument.getIssuer() == null) return Mono.just(transportDocumentSummary);
              return carrierRepository
                  .findById(transportDocument.getIssuer())
                  .flatMap(
                      carrier -> {
                        if (carrier.getSmdgCode() != null) {
                          transportDocumentSummary.setIssuerCodeListProvider(
                              CarrierCodeListProvider.SMDG);
                          transportDocumentSummary.setIssuerCode(carrier.getSmdgCode());
                        } else if (carrier.getNmftaCode() != null) {
                          transportDocumentSummary.setIssuerCodeListProvider(
                              CarrierCodeListProvider.NMFTA);
                          transportDocumentSummary.setIssuerCode(carrier.getNmftaCode());
                        } else {
                          return Mono.error(
                              ConcreteRequestErrorMessageException.invalidParameter(
                                  "Unsupported carrier code list provider."));
                        }
                        return Mono.just(transportDocumentSummary);
                      });
            });
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

  @Override
  protected TransportDocumentRepository getRepository() {
    return transportDocumentRepository;
  }
}
