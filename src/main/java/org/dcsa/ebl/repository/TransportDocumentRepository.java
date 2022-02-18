package org.dcsa.ebl.repository;

import org.dcsa.core.events.model.TransportDocument;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface TransportDocumentRepository
    extends ReactiveCrudRepository<TransportDocument, String> {

  @Query(
      "SELECT DISTINCT  s.carrier_booking_reference "
          + "FROM transport_document td "
          + "JOIN shipping_instruction si on si.id = td.shipping_instruction_id "
          + "JOIN cargo_item ci ON ci.shipping_instruction_id = si.id "
          + "JOIN shipment_equipment se ON se.id = ci.shipment_equipment_id "
          + "JOIN shipment s ON s.id = se.shipment_id "
          + "WHERE td.transport_document_reference  = :transportDocumentReference")
  Flux<String> findCarrierBookingReferenceByTransportDocumentReference(
      String transportDocumentReference);
}
