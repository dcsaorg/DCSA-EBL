package org.dcsa.ebl.extendedrequest;

import org.dcsa.core.events.model.*;
import org.dcsa.core.extendedrequest.ExtendedParameters;
import org.dcsa.core.extendedrequest.ExtendedRequest;
import org.dcsa.core.extendedrequest.QueryField;
import org.dcsa.core.query.DBEntityAnalysis;
import org.springframework.data.r2dbc.dialect.R2dbcDialect;
import org.springframework.data.relational.core.sql.Join;

public class TransportDocumentExtendedRequest<T extends TransportDocument> extends ExtendedRequest<T> {

    private static final String CARRIER_BOOKING_REFERENCE_PARAMETER = "carrierBookingReference";

    public TransportDocumentExtendedRequest(ExtendedParameters extendedParameters, R2dbcDialect r2dbcDialect, Class<T> modelClass) {
        super(extendedParameters, r2dbcDialect, modelClass);
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
                .join(Join.JoinType.JOIN, primaryModel, ShippingInstruction.class)
                .onFieldEqualsThen("shippingInstructionReference", "shippingInstructionReference")
                .chainJoin(CargoItem.class)
                .onFieldEqualsThen("shippingInstructionReference", "shippingInstructionReference")
                .chainJoin(ShipmentEquipment.class)
                .onFieldEqualsThen("shipmentEquipmentID", "id")
                .chainJoin(Shipment.class)
                .onFieldEqualsThen("shipmentID", "id")
                .registerQueryFieldFromField(CARRIER_BOOKING_REFERENCE_PARAMETER);
    }

}
