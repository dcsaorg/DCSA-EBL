package org.dcsa.ebl.extendedrequest;

import org.dcsa.core.events.model.*;
import org.dcsa.core.extendedrequest.ExtendedParameters;
import org.dcsa.core.extendedrequest.ExtendedRequest;
import org.dcsa.core.extendedrequest.QueryField;
import org.dcsa.core.extendedrequest.QueryFieldConditionGenerator;
import org.dcsa.core.query.DBEntityAnalysis;
import org.springframework.data.r2dbc.dialect.R2dbcDialect;
import org.springframework.data.relational.core.sql.Join;

public class ExtendedTransportDocumentSummaryRequest extends ExtendedRequest<TransportDocument> {
  public ExtendedTransportDocumentSummaryRequest(
      ExtendedParameters extendedParameters,
      R2dbcDialect r2dbcDialect,
      Class<TransportDocument> modelClass) {
    super(extendedParameters, r2dbcDialect, modelClass);
  }

  @Override
  protected void markQueryFieldInUse(QueryField fieldInUse) {
    super.markQueryFieldInUse(fieldInUse);
    if (fieldInUse.getJsonName().equals("carrierBookingReference")) {
      this.selectDistinct = true;
    }
  }

  @Override
  protected DBEntityAnalysis.DBEntityAnalysisBuilder<TransportDocument> prepareDBEntityAnalysis() {

    DBEntityAnalysis.DBEntityAnalysisBuilder<TransportDocument> builder =
        super.prepareDBEntityAnalysis();
    return builder
        .join(Join.JoinType.JOIN, builder.getPrimaryModelClass(), ShippingInstruction.class)
        .onFieldEqualsThen("shippingInstructionID", "shippingInstructionID")
        .registerQueryFieldFromField("documentStatus")
        .onTable(ShippingInstruction.class)
        .chainJoin(CargoItem.class)
        .onFieldEqualsThen("shippingInstructionID", "shippingInstructionID")
        .chainJoin(ShipmentEquipment.class)
        .onFieldEqualsThen("shipmentEquipmentID", "id")
        .chainJoin(Shipment.class)
        .onFieldEqualsThen("shipmentID", "shipmentID")
        .registerQueryFieldFromField("carrierBookingReference", QueryFieldConditionGenerator.inCommaSeparatedList());
  }
}