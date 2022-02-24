package org.dcsa.ebl.extendedrequest;

import org.dcsa.core.events.model.CargoItem;
import org.dcsa.core.events.model.Shipment;
import org.dcsa.core.events.model.ShipmentEquipment;
import org.dcsa.core.events.model.ShippingInstruction;
import org.dcsa.core.extendedrequest.ExtendedParameters;
import org.dcsa.core.extendedrequest.ExtendedRequest;
import org.dcsa.core.extendedrequest.QueryField;
import org.dcsa.core.query.DBEntityAnalysis;
import org.springframework.data.r2dbc.dialect.R2dbcDialect;
import org.springframework.data.relational.core.sql.Join;

public class ShippingInstructionSummariesExtendedRequest extends ExtendedRequest<ShippingInstruction> {

  private static final String CARRIER_BOOKING_REFERENCE_PARAMETER = "carrierBookingReference";

  private final String carrierBookingReference;

  public ShippingInstructionSummariesExtendedRequest(
      ExtendedParameters extendedParameters,
      R2dbcDialect r2dbcDialect,
      String carrierBookingReference) {
    super(extendedParameters, r2dbcDialect, ShippingInstruction.class);
    this.carrierBookingReference = carrierBookingReference;
  }

  @Override
  protected DBEntityAnalysis.DBEntityAnalysisBuilder<ShippingInstruction> prepareDBEntityAnalysis() {
    DBEntityAnalysis.DBEntityAnalysisBuilder<ShippingInstruction> builder = super.prepareDBEntityAnalysis();
    if (carrierBookingReference != null && !"".equals(carrierBookingReference)) {
      return builder
          .join(Join.JoinType.JOIN, builder.getPrimaryModelClass(), CargoItem.class)
          .onFieldEqualsThen("shippingInstructionID", "shippingInstructionID")
          .chainJoin(ShipmentEquipment.class)
          .onFieldEqualsThen("shipmentEquipmentID", "id")
          .chainJoin(Shipment.class)
          .onFieldEqualsThen("shipmentID", "shipmentID")
          .registerQueryFieldFromField(CARRIER_BOOKING_REFERENCE_PARAMETER);
    } else {
      return builder;
    }
  }

  @Override
  protected void markQueryFieldInUse(QueryField fieldInUse) {
    if (fieldInUse.getJsonName().equals(CARRIER_BOOKING_REFERENCE_PARAMETER)) {
      selectDistinct = true;
    }
    super.markQueryFieldInUse(fieldInUse);
  }
}
