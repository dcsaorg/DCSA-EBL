package org.dcsa.ebl.extendedrequest;

import org.dcsa.core.extendedrequest.ExtendedParameters;
import org.dcsa.core.extendedrequest.ExtendedRequest;
import org.dcsa.core.extendedrequest.FilterItem;
import org.dcsa.core.extendedrequest.Join;
import org.dcsa.ebl.model.Shipment;
import org.dcsa.ebl.model.ShippingInstruction;

public class ShippingInstructionExtendedRequest<T extends ShippingInstruction> extends ExtendedRequest<T> {

    private static final String CARRIER_BOOKING_REFERENCE_PARAMETER = "carrierBookingReference";

    private boolean alreadyJoined = false;

    public ShippingInstructionExtendedRequest(ExtendedParameters extendedParameters, Class<T> modelClass) {
        super(extendedParameters, modelClass);
        setSelectDistinct(true);
    }

    @Override
    public void resetParameters() {
        super.resetParameters();
        alreadyJoined = false;
    }

    // TODO: Fix
//    @Override
//    protected boolean doJoin(String parameter, String value, boolean fromCursor) {
//        if (CARRIER_BOOKING_REFERENCE_PARAMETER.equals(parameter)) {
//            if (!alreadyJoined) {
//                Join j = this.getJoin();
//                j.add("cargo_item ON (shipping_instruction.id=cargo_item.shipping_instruction_id)");
//                j.add("shipment_equipment ON (cargo_item.shipment_equipment_id=shipment_equipment.id)");
//                j.add("shipment ON (shipment_equipment.shipment_id=shipment.id)");
//                alreadyJoined = true;
//            }
//            filter.addFilterItem(new FilterItem(CARRIER_BOOKING_REFERENCE_PARAMETER, null, Shipment.class, value, true, false, true, false, filter.getNewBindCounter()));
//            return true;
//        }
//        return super.doJoin(parameter, value, fromCursor);
//    }
}
