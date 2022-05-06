package org.dcsa.ebl.repository;

import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface ShippingInstructionCustomRepository {
  Mono<Map<String, List<String>>> findCarrierBookingReferences(List<String> shippingInstructionReferences);
}
