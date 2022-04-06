package org.dcsa.ebl.extendedrequest;

import org.dcsa.core.events.edocumentation.model.ConsignmentItem;
import org.dcsa.core.events.model.Shipment;
import org.dcsa.core.events.model.ShippingInstruction;
import org.dcsa.core.events.model.TransportDocument;
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
        .onFieldEqualsThen("shippingInstructionID", "id")
        .registerQueryFieldFromField("documentStatus")
        .onTable(ShippingInstruction.class)
        .chainJoin(ConsignmentItem.class)
        .onFieldEqualsThen("shippingInstructionReference", "shippingInstructionID")
        .chainJoin(Shipment.class)
        .onFieldEqualsThen("shipmentID", "shipmentID")
        .registerQueryFieldFromField(
            "carrierBookingReference", QueryFieldConditionGenerator.inCommaSeparatedList());
  }
}
