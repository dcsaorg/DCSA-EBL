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

public class ShippingInstructionSummariesExtendedRequest<T extends ShippingInstruction>
    extends ExtendedRequest<T> {

  private static final String CARRIER_BOOKING_REFERENCE_PARAMETER = "carrierBookingReference";

  private final String carrierBookingReference;

  public ShippingInstructionSummariesExtendedRequest(
      String carrierBookingReference,
      ExtendedParameters extendedParameters,
      R2dbcDialect r2dbcDialect,
      Class<T> modelClass) {
    super(extendedParameters, r2dbcDialect, modelClass);
    this.carrierBookingReference = carrierBookingReference;
  }

  @Override
  protected DBEntityAnalysis.DBEntityAnalysisBuilder<T> prepareDBEntityAnalysis() {
    DBEntityAnalysis.DBEntityAnalysisBuilder<T> builder = super.prepareDBEntityAnalysis();
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
