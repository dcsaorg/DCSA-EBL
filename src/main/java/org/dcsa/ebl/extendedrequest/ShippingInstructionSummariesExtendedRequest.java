package org.dcsa.ebl.extendedrequest;

import org.dcsa.core.events.model.CargoItem;
import org.dcsa.core.events.model.Shipment;
import org.dcsa.core.events.model.ShipmentEquipment;
import org.dcsa.core.events.model.ShippingInstruction;
import org.dcsa.core.extendedrequest.ExtendedParameters;
import org.dcsa.core.extendedrequest.ExtendedRequest;
import org.dcsa.core.extendedrequest.QueryField;
import org.dcsa.core.extendedrequest.QueryFieldConditionGenerator;
import org.dcsa.core.query.DBEntityAnalysis;
import org.springframework.data.r2dbc.dialect.R2dbcDialect;
import org.springframework.data.relational.core.sql.Join;

public class ShippingInstructionSummariesExtendedRequest extends ExtendedRequest<ShippingInstruction> {

  private static final String CARRIER_BOOKING_REFERENCE_PARAMETER = "carrierBookingReference";

  public ShippingInstructionSummariesExtendedRequest(
      ExtendedParameters extendedParameters,
      R2dbcDialect r2dbcDialect) {
    super(extendedParameters, r2dbcDialect, ShippingInstruction.class);
  }

  @Override
  protected DBEntityAnalysis.DBEntityAnalysisBuilder<ShippingInstruction> prepareDBEntityAnalysis() {
    DBEntityAnalysis.DBEntityAnalysisBuilder<ShippingInstruction> builder = super.prepareDBEntityAnalysis();
    return builder
        .join(Join.JoinType.JOIN, builder.getPrimaryModelClass(), CargoItem.class)
        .onFieldEqualsThen("shippingInstructionReference", "shippingInstructionReference")
        .chainJoin(ShipmentEquipment.class)
        .onFieldEqualsThen("shipmentEquipmentID", "id")
        .chainJoin(Shipment.class)
        .onFieldEqualsThen("shipmentID", "shipmentID")
        .registerQueryFieldFromField(CARRIER_BOOKING_REFERENCE_PARAMETER, QueryFieldConditionGenerator.inCommaSeparatedList());
  }

  @Override
  protected void markQueryFieldInUse(QueryField fieldInUse) {
    if (fieldInUse.getJsonName().equals(CARRIER_BOOKING_REFERENCE_PARAMETER)) {
      selectDistinct = true;
    }
    super.markQueryFieldInUse(fieldInUse);
  }
}
