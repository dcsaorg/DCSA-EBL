package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.exception.CreateException;
import org.dcsa.core.exception.DeleteException;
import org.dcsa.core.exception.UpdateException;
import org.dcsa.core.extendedrequest.ExtendedParameters;
import org.dcsa.core.extendedrequest.ExtendedRequest;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.dcsa.ebl.model.combined.ExtendedShipmentTransport;
import org.dcsa.ebl.repository.ExtendedShipmentTransportRepository;
import org.dcsa.ebl.service.ExtendedShipmentTransportService;
import org.springframework.data.r2dbc.dialect.R2dbcDialect;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

@RequiredArgsConstructor
@Service
public class ExtendedShipmentTransportServiceImpl extends ExtendedBaseServiceImpl<ExtendedShipmentTransportRepository, ExtendedShipmentTransport, UUID> implements ExtendedShipmentTransportService {

    private final ExtendedParameters extendedParameters;

    private final ExtendedShipmentTransportRepository extendedShipmentTransportRepository;

    private final R2dbcDialect r2dbcDialect;

    @Override
    public ExtendedShipmentTransportRepository getRepository() {
        return extendedShipmentTransportRepository;
    }

    @Override
    public Flux<ExtendedShipmentTransport> findByShipmentIDOrderBySequenceNumber(UUID shipmentID) {
        ExtendedRequest<ExtendedShipmentTransport> shipmentTransportExtendedRequest = new ExtendedRequest<>(extendedParameters, r2dbcDialect, ExtendedShipmentTransport.class);
        Map<String, List<String>> params = new HashMap<>();
        params.put("shipmentID", Collections.singletonList(shipmentID.toString()));
        // Sort by SequenceNumber
        params.put("sort", Collections.singletonList("sequenceNumber"));
        shipmentTransportExtendedRequest.parseParameter(params);

        return getRepository().findAllExtended(shipmentTransportExtendedRequest);
    }

    @Override
    public Mono<ExtendedShipmentTransport> create(ExtendedShipmentTransport extendedShipmentTransport) {
        return Mono.error(new CreateException("Not possible to create Extended classes"));
    }

    @Override
    public Mono<ExtendedShipmentTransport> save(ExtendedShipmentTransport extendedShipmentTransport) {
        return Mono.error(new UpdateException("Not possible to update Extended classes"));
    }

    @Override
    public Mono<ExtendedShipmentTransport> update(ExtendedShipmentTransport update) {
        return Mono.error(new UpdateException("Not possible to update Extended classes"));
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        return Mono.error(new DeleteException("Not possible to delete Extended classes"));
    }

    @Override
    public Mono<Void> delete(ExtendedShipmentTransport extendedShipmentTransport) {
        return Mono.error(new DeleteException("Not possible to delete Extended classes"));
    }
}
