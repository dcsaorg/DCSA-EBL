package org.dcsa.ebl.repository;

import org.dcsa.core.repository.ExtendedRepository;
import org.dcsa.ebl.model.ShippingInstruction;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import reactor.core.publisher.Mono;

public interface ShippingInstructionRepository
    extends ExtendedRepository<ShippingInstruction, String>, ShippingInstructionCustomRepository {

  @Modifying
  @Query("UPDATE shipping_instruction SET place_of_issue = :placeOfIssue where id = :id")
  Mono<Boolean> setPlaceOfIssueFor(String placeOfIssue, String id);
}
