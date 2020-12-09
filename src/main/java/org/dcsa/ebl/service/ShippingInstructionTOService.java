package org.dcsa.ebl.service;

import org.dcsa.core.service.ExtendedBaseService;
import org.dcsa.ebl.model.transferobjects.*;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Flux;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

public interface ShippingInstructionTOService extends ExtendedBaseService<ShippingInstructionTO, UUID> {
    Flux<StuffingTO> updateStuffing(UUID shippingInstructionID, List<StuffingTO> stuffingList);

    Flux<EquipmentTO> updateEquipments(UUID shippingInstructionID, List<EquipmentTO> equipmentList);

    Flux<CargoItemTO> updateCargoItems(UUID shippingInstructionID, List<CargoItemTO> cargoItemList);

    Flux<DocumentPartyTO> updateParties(UUID shippingInstructionID, List<DocumentPartyTO> partyList);
}
