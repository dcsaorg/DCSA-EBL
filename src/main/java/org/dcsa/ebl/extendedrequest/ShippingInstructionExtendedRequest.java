package org.dcsa.ebl.extendedrequest;

import org.dcsa.core.extendedrequest.ExtendedParameters;
import org.dcsa.core.extendedrequest.ExtendedRequest;
import org.dcsa.core.extendedrequest.QueryField;
import org.dcsa.core.query.DBEntityAnalysis;
import org.dcsa.ebl.model.CargoItem;
import org.dcsa.ebl.model.Shipment;
import org.dcsa.ebl.model.ShipmentEquipment;
import org.dcsa.ebl.model.ShippingInstruction;
import org.springframework.data.r2dbc.dialect.R2dbcDialect;
import org.springframework.data.relational.core.sql.Join;

public class ShippingInstructionExtendedRequest<T extends ShippingInstruction> extends ExtendedRequest<T> {

    private static final String CARRIER_BOOKING_REFERENCE_PARAMETER = "carrierBookingReference";

    public ShippingInstructionExtendedRequest(ExtendedParameters extendedParameters, R2dbcDialect r2dbcDialect, Class<T> modelClass) {
        super(extendedParameters, r2dbcDialect, modelClass);
    }

    @Override
    protected DBEntityAnalysis.DBEntityAnalysisBuilder<T> prepareDBEntityAnalysis() {
        DBEntityAnalysis.DBEntityAnalysisBuilder<T> builder = super.prepareDBEntityAnalysis();
        return builder.join(Join.JoinType.JOIN, builder.getPrimaryModelClass(), CargoItem.class)
                .onEqualsThen("id", "shipping_instruction_id")
                .chainJoin(ShipmentEquipment.class)
                .onEqualsThen("shipment_equipment_id", "id")
                .chainJoin(Shipment.class)
                .onEqualsThen("shipment_id", "id")
                .registerQueryFieldFromField(CARRIER_BOOKING_REFERENCE_PARAMETER);
    }

    @Override
    protected void markQueryFieldInUse(QueryField fieldInUse) {
        if (fieldInUse.getJsonName().equals(CARRIER_BOOKING_REFERENCE_PARAMETER)) {
            selectDistinct = true;
        }
        super.markQueryFieldInUse(fieldInUse);
    }
}
