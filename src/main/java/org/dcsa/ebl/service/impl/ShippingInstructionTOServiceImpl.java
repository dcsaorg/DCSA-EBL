package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.extendedrequest.ExtendedRequest;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.dcsa.ebl.model.CargoItem;
import org.dcsa.ebl.model.CargoLineItem;
import org.dcsa.ebl.model.transferobjects.*;
import org.dcsa.ebl.repository.ShippingInstructionRepository;
import org.dcsa.ebl.repository.ShippingInstructionTORepository;
import org.dcsa.ebl.service.CargoItemService;
import org.dcsa.ebl.service.ShippingInstructionTOService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ShippingInstructionTOServiceImpl extends ExtendedBaseServiceImpl<ShippingInstructionTORepository, ShippingInstructionTO, UUID> implements ShippingInstructionTOService {

    private final ShippingInstructionTORepository shippingInstructionTORepository;
    private final ShippingInstructionRepository shippingInstructionRepository;

    private final CargoItemService cargoItemService;

    @Override
    public ShippingInstructionTORepository getRepository() {
        return shippingInstructionTORepository;
    }

    @Override
    public Class<ShippingInstructionTO> getModelClass() {
        return ShippingInstructionTO.class;
    }


    @Override
    public Flux<ShippingInstructionTO> findAllExtended(ExtendedRequest<ShippingInstructionTO> extendedRequest) {
        return super.findAllExtended(extendedRequest);
    }

    @Override
    public Mono<ShippingInstructionTO> findById(UUID id) {
        return super.findById(id);
    }

    @Override
    public Mono<ShippingInstructionTO> create(ShippingInstructionTO shippingInstructionTO) {
        return shippingInstructionRepository.save(shippingInstructionTO.getShippingInstruction())
                .map(shippingInstruction -> {
                    return ShippingInstructionTO.create(shippingInstruction);
                });
    }

    @Override
    public Mono<ShippingInstructionTO> save(ShippingInstructionTO shippingInstructionTO) {
        return super.save(shippingInstructionTO);
    }

    @Override
    public Mono<ShippingInstructionTO> update(ShippingInstructionTO update) {
        return super.update(update);
    }


    @Override
    public Flux<StuffingTO> updateStuffing(UUID shippingInstructionID, List<StuffingTO> stuffingList) {
        return null;
    }

    @Override
    public Flux<EquipmentTO> updateEquipments(UUID shippingInstructionID, List<EquipmentTO> equipmentList) {
        return null;
    }

    @Override
    public Flux<CargoItemTO> updateCargoItems(UUID shippingInstructionID, List<CargoItemTO> cargoItemList) {
        return null;
    }

    @Override
    public Flux<DocumentPartyTO> updateParties(UUID shippingInstructionID, List<DocumentPartyTO> partyList) {
        return null;
    }

    @Override
    public Mono<ShippingInstructionTO> create2(ShippingInstructionTO shippingInstructionTO) {
        return shippingInstructionRepository.save(shippingInstructionTO.getShippingInstruction())
                .map(shippingInstruction -> {
                    ShippingInstructionTO siTO = ShippingInstructionTO.create(shippingInstruction);

                    // Persist Cargo Items
                    Flux.fromIterable(shippingInstructionTO.getCargoItems())
                            .flatMap(cargoItemTO -> {
                                CargoItem cargoItem = cargoItemTO.getCargoItem();
                                cargoItem.setShippingInstructionID(shippingInstruction.getId());
                                return cargoItemService.save(cargoItem)
                                        .flatMap(cargoItem1 -> {
                                            List<CargoLineItem> cargoLineItemTOList = cargoItemTO.getCargoLineItems();

                                        });
                            });

                    CargoItem cargoItem
                    siTO.setCargoItems();
                    return Flux.concat(
                            setList(cargoItemService, siTO.getId()nhDoc.getOriginFK(), siTO::setCargoItems)
//                            List<CargoItemTO> cargoItems;
//                            List<ShipmentEquipmentTO> shipmentEquipments;
//                            List<StuffingTO> stuffing;
//                            List<DocumentPartyTO> parties;
                    ).then(Mono.just(siTO));
                })
                .flatMap(shippingInstructionX -> {
                    return shippingInstructionX;
        });
    }

//    @Override
//    public Mono<ResponseEntity<byte[]>> findNHDocumentFilebyId(Long id) {
//        NationalOrderDocumentRequest nodr = new NationalOrderDocumentRequest();
//        return nhDocRepository.findById(id)
//                .flatMap(nhDoc -> {
//                    nodr.setNhDoc(nhDoc);
//                    return Flux.concat(
//                            loadObject(customerService, nhDoc.getOriginFK(), nodr::setOrigin),
//                            loadObject(customerService, nhDoc.getReceiverFK(), nodr::setReceiver),
//                            loadObject(transporterService, nhDoc.getTransporterFK(), nodr::setTransporter)
//                            loadObject(transporterService, nhDoc.getTransporterFK(), nodr::setTransporter),
//                            loadObject(finalDispositionService, nhDoc.getFinalDispositionId(), nodr::setFinalDisposition)
//                    ).then(Mono.just(nodr));
//                }).flatMap(finalRequest -> {
//                    try {
//                        return Mono.just(nhDocumentGenerator.createNationalOrderDocumentPdf(finalRequest));
//                    } catch (Exception e) {
//                        return Mono.error(e);
//                    }
//                }).map(data -> {
//                    HttpHeaders responseHeaders = new HttpHeaders();
//                    responseHeaders.setContentType(MediaType.APPLICATION_PDF);
//                    responseHeaders.setContentDisposition(
//                            ContentDisposition.builder("attachment").filename("nhdoc-" + id + ".pdf").build()
//                    );
//                    return new ResponseEntity<>(data, responseHeaders, HttpStatus.OK);
//                });
//    }
//    private <T, I> Mono<T> loadObject(BaseService<T, I> service, I id, Consumer<T> consumer) {
//        return service.findById(id)
//                .doOnNext(consumer);
//    }
}
