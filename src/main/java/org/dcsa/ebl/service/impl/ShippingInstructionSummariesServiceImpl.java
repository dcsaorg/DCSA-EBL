package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dcsa.core.events.model.ShippingInstruction;
import org.dcsa.core.extendedrequest.ExtendedRequest;
import org.dcsa.ebl.model.mappers.ShippingInstructionSummaryMapper;
import org.dcsa.ebl.model.transferobjects.ShippingInstructionSummaryTO;
import org.dcsa.ebl.repository.ShippingInstructionRepository;
import org.dcsa.ebl.service.ShippingInstructionSummariesService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShippingInstructionSummariesServiceImpl  implements ShippingInstructionSummariesService {
  private final ShippingInstructionRepository shippingInstructionRepository;
  private final ShippingInstructionSummaryMapper shippingInstructionSummaryMapper;

  @Override
  public Flux<ShippingInstructionSummaryTO> findShippingInstructions(ExtendedRequest<ShippingInstruction> request) {
    Flux<ShippingInstructionSummaryTO> summaries =
      shippingInstructionRepository.findAllExtended(request).concatMap(this::mapDaoToDto);
    return populateCarrierBookingReferences(summaries);
  }

  private Flux<ShippingInstructionSummaryTO> populateCarrierBookingReferences(Flux<ShippingInstructionSummaryTO> summaries) {
    log.debug("populateCarrierBookingReferences");

    return summaries
      .collectList()
      .map(shippingInstructions -> shippingInstructionRepository.findCarrierBookingReferences(
        shippingInstructions.stream().map(si -> si.getShippingInstructionID()).collect(Collectors.toList())
      ).map(map -> shippingInstructions.stream()
        .map(si -> si.withCarrierBookingReferences(map.getOrDefault(si.getShippingInstructionID(), Collections.emptyList())))
        .collect(Collectors.toList())
      ))
      .flatMap(Function.identity())
      .flatMapMany(Flux::fromIterable);
  }

  private Mono<ShippingInstructionSummaryTO> mapDaoToDto(ShippingInstruction instruction) {
    return Mono.just(shippingInstructionSummaryMapper.shippingInstructionToDTO(instruction));
  }
}
