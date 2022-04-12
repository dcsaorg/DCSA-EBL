package org.dcsa.ebl.model.mappers;

import org.dcsa.core.events.model.ShippingInstruction;
import org.dcsa.ebl.model.transferobjects.ShippingInstructionSummaryTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ShippingInstructionSummaryMapper {
  @Mapping(source = "shippingInstructionCreatedDateTime", target = "shippingInstructionCreatedDateTime")
  @Mapping(source = "shippingInstructionUpdatedDateTime", target = "shippingInstructionUpdatedDateTime")
  ShippingInstructionSummaryTO shippingInstructionToDTO(ShippingInstruction shippingInstruction);
}
