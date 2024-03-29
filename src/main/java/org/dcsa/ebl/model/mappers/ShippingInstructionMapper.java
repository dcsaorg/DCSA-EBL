package org.dcsa.ebl.model.mappers;

import org.dcsa.core.events.model.ShippingInstruction;
import org.dcsa.core.events.model.transferobjects.ShippingInstructionTO;
import org.dcsa.ebl.model.transferobjects.ShippingInstructionResponseTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ShippingInstructionMapper {
    @Mapping(source = "placeOfIssueID", target = "placeOfIssue.id", ignore = true)
    ShippingInstructionTO shippingInstructionToDTO(ShippingInstruction shippingInstruction);

    @Mapping(source = "placeOfIssue", target = "placeOfIssueID", ignore = true)
    ShippingInstruction dtoToShippingInstruction(ShippingInstructionTO shippingInstructionTO);

    ShippingInstructionResponseTO shippingInstructionToShippingInstructionResponseTO(ShippingInstruction shippingInstruction);

    ShippingInstructionResponseTO dtoToShippingInstructionResponseTO(ShippingInstructionTO shippingInstructionTO);
}
