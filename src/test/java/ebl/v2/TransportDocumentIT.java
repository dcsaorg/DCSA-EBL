package ebl.v2;

import ebl.config.TestConfig;
import org.apache.http.HttpStatus;
import org.dcsa.core.events.model.TransportDocument;
import org.dcsa.core.events.model.enums.LocationType;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.ebl.model.TransportDocumentSummary;
import org.dcsa.ebl.model.transferobjects.ShippingInstructionResponseTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ebl.config.TestConfig.*;
import static ebl.config.TestUtil.loadFileAsString;
import static io.restassured.RestAssured.given;
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
        .contentType("application/json")
        .get(TRANSPORT_DOCUMENT_SUMMARIES)
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_OK)
        .body("size()", greaterThanOrEqualTo(0))
        .body("transportDocumentReference", anyOf(hasItem("9b02401c-b2fb-5009")))
        .body(jsonSchemaValidator("transportDocument"));
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
    String transportDocumentReference = "c90a0ed6-ccc9-48e3";

    System.out.println("Resetting transport document!");
    given()
        .contentType("application/json")
        .post("v2/unofficial/reset-transport-document/" + transportDocumentReference)
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_OK);

    System.out.println("Approving transport document!");
    given()
        .contentType("application/json")
        .body("{ \"documentStatus\": \"APPR\" }")
        .patch(TRANSPORT_DOCUMENTS + "/" + transportDocumentReference)
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_OK)
        .body("transportDocumentReference", equalTo(transportDocumentReference))
        .body("documentStatus", equalTo(ShipmentEventTypeCode.APPR.toString()));
  }

  @Test
  void testApproveInvalidTransportDocument() {
    TransportDocument td = new TransportDocument();
    td.setId(UUID.fromString("cf48ad0a-9a4b-48a7-b752-c248fb5d88d9"));
    td.setTransportDocumentReference("c90a0ed6-ccc9-48e3");

    System.out.println("Resetting transport document!");
    given()
        .contentType("application/json")
        .post("/v2/unofficial/reset-transport-document/" + td.getId())
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_OK);

    System.out.println("Approving transport document!");
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
  void testApproveNotFoundTransportDocument() {
    String transportDocumentReference = UUID.randomUUID().toString().substring(0, 12);
    given()
        .contentType("application/json")
        .post("v2/unofficial/reset-transport-document/" + transportDocumentReference)
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_NOT_FOUND)
        .body("errors[0].reason", equalTo("notFound"))
        .body(
            "errors[0].message",
            containsString(
                "No transport document found with transport document reference: "
                    + transportDocumentReference))
        .body(jsonSchemaValidator("error"))
        .extract()
        .asString();
  }
}
