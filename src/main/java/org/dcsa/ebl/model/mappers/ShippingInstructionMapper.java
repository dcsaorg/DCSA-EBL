package org.dcsa.ebl.model.mappers;

import org.dcsa.ebl.model.ShippingInstruction;
import org.dcsa.ebl.model.transferobjects.ShippingInstructionResponseTO;
import org.dcsa.ebl.model.transferobjects.ShippingInstructionTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ShippingInstructionMapper {
    ShippingInstructionTO shippingInstructionToDTO(ShippingInstruction shippingInstruction);

    ShippingInstruction dtoToShippingInstruction(ShippingInstructionTO shippingInstructionTO);

    ShippingInstructionResponseTO shippingInstructionToShippingInstructionResponseTO(ShippingInstruction shippingInstruction);

    ShippingInstructionResponseTO dtoToShippingInstructionResponseTO(ShippingInstructionTO shippingInstructionTO);
}
