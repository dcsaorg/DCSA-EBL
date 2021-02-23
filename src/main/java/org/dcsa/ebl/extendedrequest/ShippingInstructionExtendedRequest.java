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
    }

    @Override
    public void resetParameters() {
        super.resetParameters();
        alreadyJoined = false;
    }

    // FIXME: Have super class support DISTINCT natively.
    // (We need this to avoid repeating the same ShippingInstruction when it references the same
    // Shipments multiple times)
    @Override
    public void getTableFields(StringBuilder sb) {
        sb.append("DISTINCT shipping_instruction.*");
    }

    // FIXME: Have super class support DISTINCT natively.
    @Override
    public String getCountQuery() {
        return super.getCountQuery().replace("count(*)", "DISTINCT count(*)");
    }


    @Override
    protected boolean doJoin(String parameter, String value, boolean fromCursor) {
        if (CARRIER_BOOKING_REFERENCE_PARAMETER.equals(parameter)) {
            if (!alreadyJoined) {
                Join j = this.getJoin();
                j.add("cargo_item ON (shipping_instruction.id=cargo_item.shipping_instruction_id)");
                j.add("shipment_equipment ON (cargo_item.shipment_equipment_id=shipment_equipment.id)");
                j.add("shipment ON (shipment_equipment.shipment_id=shipment.id)");
                alreadyJoined = true;
            }
            filter.addFilterItem(new FilterItem(CARRIER_BOOKING_REFERENCE_PARAMETER, null, Shipment.class, value, true, false, true, false, filter.getNewBindCounter()));
            return true;
        }
        return super.doJoin(parameter, value, fromCursor);
    }
}
