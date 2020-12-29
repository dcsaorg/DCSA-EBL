package org.dcsa.ebl.service;

import com.github.fge.jsonpatch.JsonPatch;
import org.dcsa.core.extendedrequest.ExtendedRequest;
import org.dcsa.ebl.model.ShippingInstruction;
import org.dcsa.ebl.model.transferobjects.ShippingInstructionTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ShippingInstructionTOService {

    Mono<ShippingInstructionTO> create(ShippingInstructionTO shippingInstructionTO);
    Mono<ShippingInstructionTO> findById(UUID uuid);
    Mono<ShippingInstructionTO> patchOriginal(UUID uuid, JsonPatch patch);
    Mono<ShippingInstructionTO> replaceOriginal(UUID shippingInstructionId, ShippingInstructionTO update);
    // Use of ShippingInstruction is deliberate because we do not support filtering on any other fields then
    // those provided in ShippingInstruction.class
    Flux<ShippingInstructionTO> findAllExtended(final ExtendedRequest<ShippingInstruction> extendedRequest);
}
