package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dcsa.core.events.model.ShippingInstruction;
import org.dcsa.core.events.model.base.AbstractShippingInstruction;
import org.dcsa.core.service.impl.AsymmetricQueryServiceImpl;
import org.dcsa.ebl.model.mappers.ShippingInstructionSummaryMapper;
import org.dcsa.ebl.model.transferobjects.ShippingInstructionSummaryTO;
import org.dcsa.ebl.repository.ShippingInstructionRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShippingInstructionSummariesServiceImpl extends AsymmetricQueryServiceImpl<ShippingInstructionRepository, ShippingInstruction, ShippingInstructionSummaryTO, String> {
  private final ShippingInstructionRepository shippingInstructionRepository;
  private final ShippingInstructionSummaryMapper shippingInstructionSummaryMapper;

  @Override
  protected Flux<ShippingInstructionSummaryTO> bulkMapDM2TO(Flux<ShippingInstruction> dmFlux) {
    return convertAndPopulateShippingInstructions(dmFlux);
  }

  @Override
  protected Mono<ShippingInstructionSummaryTO> mapDM2TO(ShippingInstruction shippingInstruction) {
    return null; // dummy - isn't used but need to be implemented
  }

  @Override
  protected ShippingInstructionRepository getRepository() {
    return shippingInstructionRepository;
  }

  private Flux<ShippingInstructionSummaryTO> convertAndPopulateShippingInstructions(Flux<ShippingInstruction> instructions) {
    log.debug("convertAndPopulateShippingInstructions");

    return instructions
      .collectList()
      .map(shippingInstructions -> shippingInstructionRepository
        .findCarrierBookingReferences(shippingInstructions.stream().map(AbstractShippingInstruction::getShippingInstructionID).collect(Collectors.toList()))
        .map(map -> shippingInstructions.stream()
          .map(si -> mapDaoToDto(si, map.getOrDefault(si.getShippingInstructionID(), Collections.emptyList())))
          .collect(Collectors.toList())
      ))
      .flatMap(Function.identity())
      .flatMapMany(Flux::fromIterable);
  }

  private ShippingInstructionSummaryTO mapDaoToDto(ShippingInstruction instruction, List<String> carrierBookingReferences) {
    ShippingInstructionSummaryTO dto = shippingInstructionSummaryMapper.shippingInstructionToDTO(instruction);
    dto.setCarrierBookingReferences(carrierBookingReferences);
    return dto;
  }
}
