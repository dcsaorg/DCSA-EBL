package ebl.v2;

import ebl.config.TestConfig;
import org.apache.http.HttpStatus;
import org.dcsa.core.events.model.TransportDocument;
import org.dcsa.core.events.model.enums.LocationType;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.UUID;

import static ebl.config.TestConfig.*;
import static io.restassured.RestAssured.given;
import static org.dcsa.core.events.model.enums.ShipmentEventTypeCode.*;
import static org.hamcrest.Matchers.*;

public class TransportDocumentIT {

  @BeforeAll
  static void configs() throws IOException {
    TestConfig.init();
  }

  @Test
  void testValidTDIsInSummary() {
    // Test that the valid transport document exists in data set.
    given()
        .queryParam("limit", 1000)
        .contentType("application/json")
        .get(TRANSPORT_DOCUMENT_SUMMARIES)
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_OK)
        .body("size()", greaterThanOrEqualTo(0))
        .body("transportDocumentReference", anyOf(hasItem("9b02401c-b2fb-5009")))
        .body(jsonSchemaValidator("transportDocumentSummary"));
  }

  @Test
  void testValidGetTransportDocument() {

    given()
        .contentType("application/json")
        .get(TRANSPORT_DOCUMENTS + "/" + "9b02401c-b2fb-5009")
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_OK)
        .body("transportDocumentReference", notNullValue())
        .body("shippingInstruction", notNullValue())
        .body(jsonSchemaValidator("transportDocument"));
  }

  @Test
  void testGetNotFoundTransportDocument() {

    given()
        .contentType("application/json")
        .get(TRANSPORT_DOCUMENTS + "/" + "DoesNotExists")
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_NOT_FOUND)
        .body("httpMethod", equalTo("GET"))
        .body("requestUri", containsString("/v2/transport-documents/"))
        .body("errors[0].reason", equalTo("notFound"))
        .body(
            "errors[0].message",
            containsString("No transport document found with transport document reference"))
        .body("statusCode", equalTo(404))
        .body("statusCodeText", equalTo("Not Found"))
        .extract()
        .body()
        .asString();
  }

  @Test
  void testShipmentLocationsInTransportDocument() {

    // This is to test shipmentLocationRepository.findByTransportDocumentID query
    // (CI is not configured in Event-Core to test against database)
    given()
        .contentType("application/json")
        .get(TRANSPORT_DOCUMENTS + "/" + "0cc0bef0-a7c8-4c03")
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_OK)
        .body("transportDocumentReference", notNullValue())
        .body("shippingInstruction", notNullValue())
        .body("shippingInstruction.shippingInstructionReference", equalTo("SI_REF_9"))
        .body("shipmentLocations.size()", equalTo(3))
        .body(
            "shipmentLocations.flatten().findAll { it.location.locationName == 'Copenhagen' }.size()",
            equalTo(1))
        .body(
            "shipmentLocations.flatten().findAll { it.location.locationName == 'Orlando' }.size()",
            equalTo(1))
        .body(
            "shipmentLocations.flatten().findAll { it.location.locationName == 'Miami' }.size()",
            equalTo(1))
        .body(
            "shipmentLocations.flatten().findAll { it.location.locationName == 'Copenhagen' }.shipmentLocationTypeCode",
            everyItem(equalTo(LocationType.PRE.toString())))
        .body(
            "shipmentLocations.flatten().findAll { it.location.locationName == 'Orlando' }.shipmentLocationTypeCode",
            everyItem(equalTo(LocationType.POL.toString())))
        .body(
            "shipmentLocations.flatten().findAll { it.location.locationName == 'Miami' }.shipmentLocationTypeCode",
            everyItem(equalTo(LocationType.POD.toString())))
        .body(jsonSchemaValidator("transportDocument"));
  }

  @Test
  void testApproveValidTransportDocument() {
    TransportDocument td = new TransportDocument();
    td.setId(UUID.fromString("cf48ad0a-9a4b-48a7-b752-c248fb5d88d9"));
    td.setTransportDocumentReference("c90a0ed6-ccc9-48e3");

    given()
        .contentType("application/json")
        .post("/v2/unofficial/change-document-status-by-transport-document/" + td.getId())
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_OK);

    given()
        .contentType("application/json")
        .body("{ \"documentStatus\": \"APPR\" }")
        .patch(TRANSPORT_DOCUMENTS + "/" + td.getTransportDocumentReference())
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_OK)
        .body("transportDocumentReference", equalTo(td.getTransportDocumentReference()))
        .body("documentStatus", equalTo(ShipmentEventTypeCode.APPR.toString()));
  }

  @Test
  void testAllInvalidBookingDocumentStatusOnApproveTransportDocument() {
    TransportDocument td = new TransportDocument();
    td.setId(UUID.fromString("cf48ad0a-9a4b-48a7-b752-c248fb5d88d9"));
    td.setTransportDocumentReference("c90a0ed6-ccc9-48e3");

    for (String b : BOOKING_DOCUMENT_STATUSES.split(",")) {
      ShipmentEventTypeCode bookingDocumentStatus = ShipmentEventTypeCode.valueOf(b);
      if (bookingDocumentStatus == CONF) continue;

      given()
          .contentType("application/json")
          .queryParam("bookingStatus", bookingDocumentStatus)
          .post("/v2/unofficial/change-document-status-by-transport-document/" + td.getId())
          .then()
          .assertThat()
          .statusCode(HttpStatus.SC_OK);

      given()
          .contentType("application/json")
          .body("{ \"documentStatus\": \"APPR\" }")
          .patch(TRANSPORT_DOCUMENTS + "/" + td.getTransportDocumentReference())
          .then()
          .assertThat()
          .body(
              "errors[0].message",
              equalTo(
                  "DocumentStatus "
                      + bookingDocumentStatus
                      + " for booking KUBERNETES_IN_ACTION_03 related to carrier booking reference c90a0ed6-ccc9-48e3 is not in CONF state!"))
          .body("errors[0].reason", equalTo("invalidParameter"))
          .body(jsonSchemaValidator("error"));
    }
  }

  @Test
  void testAllInvalidShippingInstructionDocumentStatusOnApproveTransportDocument() {
    TransportDocument td = new TransportDocument();
    td.setId(UUID.fromString("cf48ad0a-9a4b-48a7-b752-c248fb5d88d9"));
    td.setTransportDocumentReference("c90a0ed6-ccc9-48e3");

    for (String si : EBL_DOCUMENT_STATUSES.split(",")) {
      ShipmentEventTypeCode documentStatus = ShipmentEventTypeCode.valueOf(si);
      if (documentStatus == DRFT) continue;
      given()
          .contentType("application/json")
          .queryParam("shippingInstructionStatus", documentStatus)
          .post("/v2/unofficial/change-document-status-by-transport-document/" + td.getId())
          .then()
          .assertThat()
          .statusCode(HttpStatus.SC_OK);

      given()
          .contentType("application/json")
          .body("{ \"documentStatus\": \"APPR\" }")
          .patch(TRANSPORT_DOCUMENTS + "/" + td.getTransportDocumentReference())
          .then()
          .assertThat()
          .statusCode(HttpStatus.SC_BAD_REQUEST)
          .body(
              "errors[0].message",
              equalTo(
                  "Cannot Approve Transport Document with Shipping Instruction that is not in status DRFT"))
          .body("errors[0].reason", equalTo("invalidParameter"))
          .body(jsonSchemaValidator("error"));
    }
  }

  @Test
  void testApproveNotFoundTransportDocument() {
    UUID transportDocumentId = UUID.randomUUID();
    given()
        .contentType("application/json")
        .post("v2/unofficial/change-document-status-by-transport-document/" + transportDocumentId)
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_NOT_FOUND)
        .body("errors[0].reason", equalTo("notFound"))
        .body(
            "errors[0].message",
            containsString(
                "No transport document found with transport document id: " + transportDocumentId))
        .body(jsonSchemaValidator("error"))
        .extract()
        .asString();
  }
}
