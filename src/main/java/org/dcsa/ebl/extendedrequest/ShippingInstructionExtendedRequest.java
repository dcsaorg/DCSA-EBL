package org.dcsa.ebl.extendedrequest;

import org.dcsa.core.extendedrequest.*;
import org.dcsa.core.query.DBEntityAnalysis;
import org.dcsa.ebl.model.CargoItem;
import org.dcsa.ebl.model.Shipment;
import org.dcsa.ebl.model.ShipmentEquipment;
import org.dcsa.ebl.model.ShippingInstruction;
import org.springframework.data.relational.core.sql.Join;

public class ShippingInstructionExtendedRequest<T extends ShippingInstruction> extends ExtendedRequest<T> {

    private static final String CARRIER_BOOKING_REFERENCE_PARAMETER = "carrierBookingReference";

    public ShippingInstructionExtendedRequest(ExtendedParameters extendedParameters, Class<T> modelClass) {
        super(extendedParameters, modelClass);
        setSelectDistinct(true);
    }

    @Override
    protected void markQueryFieldInUse(QueryField fieldInUse) {
        super.markQueryFieldInUse(fieldInUse);
        selectDistinct = selectDistinct || fieldInUse.getJsonName().equals(CARRIER_BOOKING_REFERENCE_PARAMETER);
    }

    @Override
    protected DBEntityAnalysis.DBEntityAnalysisBuilder<T> prepareDBEntityAnalysis() {
        DBEntityAnalysis.DBEntityAnalysisBuilder<T> builder = super.prepareDBEntityAnalysis();
        Class<?> primaryModel = builder.getPrimaryModelClass();
        return builder
                .join(Join.JoinType.JOIN, primaryModel, CargoItem.class)
                .onFieldEqualsThen("id", "shippingInstructionID")
                .chainJoin(ShipmentEquipment.class)
                .onFieldEqualsThen("shipmentEquipmentID", "id")
                .chainJoin(Shipment.class)
                .onFieldEqualsThen("shipmentID", "id")
                .registerQueryFieldFromField(CARRIER_BOOKING_REFERENCE_PARAMETER);
    }

}
