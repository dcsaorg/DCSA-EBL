package org.dcsa.ebl.service.impl;

import org.dcsa.core.events.model.ShippingInstruction;
import org.dcsa.ebl.model.mappers.ShippingInstructionSummaryMapper;
import org.dcsa.ebl.model.transferobjects.ShippingInstructionSummaryTO;
import org.dcsa.ebl.repository.ShippingInstructionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests for ShippingInstructionSummaries Implementation.")
public class ShippingInstructionSummariesServiceImplTest {

  @Mock
  private ShippingInstructionRepository shippingInstructionRepository;

  private ShippingInstructionSummaryMapper shippingInstructionSummaryMapper = Mappers.getMapper(ShippingInstructionSummaryMapper.class);

  private List<ShippingInstruction> shippingInstructions;
  private Map<String, List<String>> instructions2references;

  private ShippingInstructionSummariesServiceImpl service;

  @BeforeEach
  public void init() {
    shippingInstructions = new ArrayList<>();
    instructions2references = new HashMap<>();
    service = new ShippingInstructionSummariesServiceImpl(shippingInstructionRepository, shippingInstructionSummaryMapper);

    when(shippingInstructionRepository.findCarrierBookingReferences(any()))
      .thenReturn(Mono.just(instructions2references));
  }

  @Test
  @DisplayName("mappings from ShippingInstruction to ShippingInstructionSummary get the correct carrierBookingReferences")
  public void testMapping() {
    buildShippingInstruction("si-1", null);
    buildShippingInstruction("si-2", List.of("cbr-2-1"));
    buildShippingInstruction("si-3", List.of("cbr-3-1", "cbr-3-2"));

    Flux<ShippingInstructionSummaryTO> summaries = service.bulkMapDM2TO(Flux.fromIterable(shippingInstructions));

    StepVerifier.create(summaries)
      .expectNext(createShippingInstructionSummaryTO("si-1", Collections.emptyList()))
      .expectNext(createShippingInstructionSummaryTO("si-2", List.of("cbr-2-1")))
      .expectNext(createShippingInstructionSummaryTO("si-3", List.of("cbr-3-1", "cbr-3-2")))
      .verifyComplete();
  }

  private void buildShippingInstruction(String id, List<String> carrierBookingReferences) {
    ShippingInstruction si = new ShippingInstruction();
    si.setShippingInstructionReference(id);

    shippingInstructions.add(si);
    if (carrierBookingReferences != null) {
      instructions2references.put(id, carrierBookingReferences);
    }
  }

  private ShippingInstructionSummaryTO createShippingInstructionSummaryTO(String id, List<String> carrierBookingReferences) {
    return ShippingInstructionSummaryTO.builder()
      .shippingInstructionReference(id)
      .carrierBookingReferences(carrierBookingReferences)
      .build();
  }
}
