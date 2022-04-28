package org.dcsa.ebl.repository;

import org.dcsa.core.events.model.ShippingInstruction;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.core.repository.ExtendedRepository;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface ShippingInstructionRepository
    extends ExtendedRepository<ShippingInstruction, UUID>, ShippingInstructionCustomRepository {

  Mono<ShippingInstruction> findByShippingInstructionReference(String shippingInstructionReference);

  @Modifying
  @Query("UPDATE shipping_instruction SET place_of_issue = :placeOfIssue where id = :id")
  Mono<Boolean> setPlaceOfIssueFor(String placeOfIssue, UUID id);

  // TODO DDT-994
  @Query(
      "SELECT DISTINCT ci.shipment_id FROM shipping_instruction si "
          + "JOIN consignment_item ci ON ci.shipping_instruction_id = si.id "
          + "WHERE si.id = :shippingInstructionID")
  Flux<UUID> findShipmentIDsByShippingInstructionID(UUID shippingInstructionID);

  @Query(
      "SELECT DISTINCT s.carrier_booking_reference FROM shipping_instruction si "
          + "JOIN consignment_item ci ON ci.shipping_instruction_id = si.id "
          + "JOIN shipment s ON s.id = ci.shipment_id "
          + "WHERE si.id = :shippingInstructionID")
  Flux<String> findCarrierBookingReferenceByShippingInstructionID(UUID shippingInstructionID);

  @Modifying
  @Query(
    "UPDATE shipping_instruction SET document_status = :documentStatus, updated_date_time = :updatedDateTime"
      + " where shipping_instruction_reference = :reference AND valid_until IS NULL")
  Mono<Boolean> setDocumentStatusByReference(
    ShipmentEventTypeCode documentStatus, OffsetDateTime updatedDateTime, String reference);

  @Query(
      "SELECT si.* FROM shipping_instrution si "
          + "JOIN transport_document td ON (si.id = td.shipping_instruction_id) "
          + "WHERE td.id = :transportDocumentID")
  Mono<ShippingInstruction> findByTransportDocumentID(UUID transportDocumentID);
}
