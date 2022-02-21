package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.events.edocumentation.service.CarrierClauseService;
import org.dcsa.core.events.edocumentation.service.ChargeService;
import org.dcsa.core.events.model.Carrier;
import org.dcsa.core.events.model.TransportDocument;
import org.dcsa.core.events.model.enums.CarrierCodeListProvider;
import org.dcsa.core.events.repository.CarrierRepository;
import org.dcsa.core.events.repository.TransportDocumentRepository;
import org.dcsa.core.events.service.LocationService;
import org.dcsa.core.exception.ConcreteRequestErrorMessageException;
import org.dcsa.core.service.impl.QueryServiceImpl;
import org.dcsa.ebl.model.mappers.TransportDocumentMapper;
import org.dcsa.ebl.model.transferobjects.TransportDocumentTO;
import org.dcsa.ebl.service.ShippingInstructionService;
import org.dcsa.ebl.service.TransportDocumentService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;

@RequiredArgsConstructor
@Service
public class TransportDocumentServiceImpl
    extends QueryServiceImpl<TransportDocumentRepository, TransportDocument, String>
    implements TransportDocumentService {

  private final TransportDocumentRepository transportDocumentRepository;
  private final CarrierRepository carrierRepository;
  private final ShippingInstructionService shippingInstructionService;
  private final ChargeService chargeService;
  private final CarrierClauseService carrierClauseService;
  private final LocationService locationService;

  private final TransportDocumentMapper transportDocumentMapper;

  @Override
  public TransportDocumentRepository getRepository() {
    return transportDocumentRepository;
  }

  @Override
  public Mono<TransportDocumentTO> findByTransportDocumentReference(
      String transportDocumentReference) {
    return Mono.justOrEmpty(transportDocumentReference)
        .flatMap(transportDocumentRepository::findById)
        .flatMap(
            transportDocument -> {
              TransportDocumentTO transportDocumentTO =
                  transportDocumentMapper.transportDocumentToDTO(transportDocument);
              return Mono.when(
                      carrierRepository
                          .findById(transportDocument.getIssuer())
                          .doOnNext(
                              carrier -> {
                                setIssuerOnTransportDocument(transportDocumentTO, carrier);
                              }),
                      locationService
                          .fetchLocationDeepObjByID(transportDocument.getPlaceOfIssue())
                          .doOnNext(transportDocumentTO::setPlaceOfIssue),
                      shippingInstructionService
                          .findById(transportDocument.getShippingInstructionID())
                          .switchIfEmpty(
                              Mono.error(
                                  ConcreteRequestErrorMessageException.notFound(
                                      "No shipping instruction found with shipping instruction id: "
                                          + transportDocument.getShippingInstructionID())))
                          .doOnNext(transportDocumentTO::setShippingInstruction),
                      chargeService
                          .fetchChargesByTransportDocumentReference(transportDocumentReference)
                          .collectList()
                          .doOnNext(transportDocumentTO::setCharges),
                      carrierClauseService
                          .fetchCarrierClausesByTransportDocumentReference(
                              transportDocumentReference)
                          .collectList()
                          .doOnNext(transportDocumentTO::setClauses))
                  .thenReturn(transportDocumentTO);
            });
  }

  void setIssuerOnTransportDocument(TransportDocumentTO transportDocumentTO, Carrier carrier) {
    if (Objects.nonNull(carrier.getSmdgCode())) {
      transportDocumentTO.setIssuerCode(carrier.getSmdgCode());
      transportDocumentTO.setIssuerCodeListProvider(CarrierCodeListProvider.SMDG);
    } else if (Objects.nonNull(carrier.getNmftaCode())) {
      transportDocumentTO.setIssuerCode(carrier.getNmftaCode());
      transportDocumentTO.setIssuerCodeListProvider(CarrierCodeListProvider.NMFTA);
    }
  }
}
