package org.dcsa.ebl.repository;

import org.dcsa.core.repository.ExtendedRepository;
import org.dcsa.ebl.model.DisplayedAddress;
import org.dcsa.ebl.model.enums.PartyFunction;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface DisplayedAddressRepository extends ExtendedRepository<DisplayedAddress, UUID> {

    @Modifying
    @Query("UPDATE displayed_address SET shipping_instruction_id = NULL"
            + " WHERE shipping_instruction_id = :shippingInstructionID"
            + "   AND shipment_id IS NOT NULL"
    )
    Mono<Integer> clearShippingInstructionIDWhereShipmentIDIsNotNull(String shippingInstructionID);


    Mono<Void> deleteByShippingInstructionIDAndShipmentIDIsNull(String shippingInstructionID);

    Flux<DisplayedAddress> findByPartyIDAndPartyFunctionAndShippingInstructionIDAndShipmentIDOrderByAddressLineNumberAsc(
            String partyID,
            PartyFunction partyFunction,
            String shippingInstructionID,
            UUID shipmentID
    );
}
