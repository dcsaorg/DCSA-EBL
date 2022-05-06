package ebl.v2;

import com.fasterxml.jackson.databind.ObjectMapper;
import ebl.config.TestConfig;
import org.apache.http.HttpStatus;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.ebl.model.TransportDocumentSummary;
import org.dcsa.ebl.model.transferobjects.ShippingInstructionResponseTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ebl.config.TestConfig.*;
import static ebl.config.TestUtil.jsonToMap;
import static ebl.config.TestUtil.loadFileAsString;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class TransportDocumentSummariesIT {

  private final String VALID_SHIPPING_INSTRUCTION =
      loadFileAsString("ValidShippingInstruction.json");
  private final String VALID_SHIPPING_INSTRUCTION_MULTIPLE_CARRIER_BOOKING_REFERENCES =
      loadFileAsString("ValidShippingInstructionMultipleCarrierBookingReferences.json");

  @BeforeAll
  static void configs() throws IOException {
    TestConfig.init();
  }

  @Test
  void testValidGetTransportDocumentSummariesWithoutParameters() {

    given()
        .contentType("application/json")
        .get(TRANSPORT_DOCUMENT_SUMMARIES)
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_OK)
        .body("size()", greaterThan(0))
        .body("shippingInstructionReference", everyItem(notNullValue()))
        .body("transportDocumentReference", everyItem(notNullValue()))
        .body("documentStatus", everyItem(notNullValue()))
        .body("transportDocumentCreatedDateTime", everyItem(notNullValue()))
        .body("transportDocumentUpdatedDateTime", everyItem(notNullValue()))
        .body(jsonSchemaValidator("transportDocumentSummary"));
  }

  @Test
  void testValidGetTransportDocumentSummariesWithSingleCarrierBookingReference() {

    Map<String, Object> map = jsonToMap(VALID_SHIPPING_INSTRUCTION);
    assert map != null;
    map.put("carrierBookingReference", "e8e9d64172934a40aec82e4308cdf97a");
    createShippingInstruction(map);

    given()
        .contentType("application/json")
        .get(
            TRANSPORT_DOCUMENT_SUMMARIES
                + "?carrierBookingReference="
                + map.get("carrierBookingReference"))
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_OK)
        .body("size()", greaterThan(0))
        .body("carrierBookingReferences", everyItem(hasItem("e8e9d64172934a40aec82e4308cdf97a")))
        .body(jsonSchemaValidator("transportDocumentSummary"));
  }

  @Test
  void testValidGetTransportDocumentSummariesWithSingleCarrierBookingReferenceAndDocumentStatus() {

    Map<String, Object> map = jsonToMap(VALID_SHIPPING_INSTRUCTION);
    assert map != null;
    map.put("carrierBookingReference", "e8e9d64172934a40aec82e4308cdf97a");
    createShippingInstruction(map);

    given()
        .contentType("application/json")
        .get(
            TRANSPORT_DOCUMENT_SUMMARIES
                + "?carrierBookingReference="
                + map.get("carrierBookingReference")
                + "&documentStatus="
                + ShipmentEventTypeCode.DRFT)
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_OK)
        .body("size()", greaterThan(0))
        .body("[0].carrierBookingReferences.size()", is(1))
        .body("[0].carrierBookingReferences", hasItem("e8e9d64172934a40aec82e4308cdf97a"))
        .body("[0].transportDocumentReference", notNullValue())
        .body("[0].shippingInstructionReference", notNullValue())
        .body("[0].documentStatus", equalTo(String.valueOf(ShipmentEventTypeCode.DRFT)))
        .body("[0].transportDocumentCreatedDateTime", notNullValue())
        .body("[0].transportDocumentUpdatedDateTime", notNullValue());

    // Ensure that documentStatus query is respected, should return zero elements
    given()
        .contentType("application/json")
        .get(
            TRANSPORT_DOCUMENT_SUMMARIES
                + "?carrierBookingReference="
                + map.get("carrierBookingReference")
                + "&documentStatus="
                + ShipmentEventTypeCode.RECE)
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_OK)
        .body("size()", is(0));
  }

  @Test
  void testValidGetTransportDocumentSummariesWithMultipleCarrierBookingReferences() {

    Map<String, Object> map =
        jsonToMap(VALID_SHIPPING_INSTRUCTION_MULTIPLE_CARRIER_BOOKING_REFERENCES);
    assert map != null;
    createShippingInstruction(map);

    String[] carrierBookingReferences = {
      "bca68f1d3b804ff88aaa1e43055432f7", "e8e9d64172934a40aec82e4308cdf97a"
    };

    TransportDocumentSummary[] response =
        given()
            .contentType("application/json")
            .get(
                TRANSPORT_DOCUMENT_SUMMARIES
                    + "?carrierBookingReference="
                    + String.join(",", carrierBookingReferences))
            .then()
            .assertThat()
            .statusCode(HttpStatus.SC_OK)
            .body("size()", greaterThan(0))
            .body(jsonSchemaValidator("transportDocumentSummary"))
            .extract()
            .body()
            .as(TransportDocumentSummary[].class);

    List<String> carrierBookingReferenceResults =
        Arrays.stream(response)
            .map(TransportDocumentSummary::getCarrierBookingReferences)
            .flatMap(List::stream)
            .distinct()
            .collect(Collectors.toList());

    assert carrierBookingReferenceResults.contains(carrierBookingReferences[0]);
    assert carrierBookingReferenceResults.contains(carrierBookingReferences[1]);
  }

  @Test
  void testValidGetTransportDocumentSummariesWithCarrierBookingReferenceAndDocumentStatus() {

    ShipmentEventTypeCode documentStatus = ShipmentEventTypeCode.RECE;
    String carrierBookingReference = "castlesofronburgundy";

    given()
        .contentType("application/json")
        .get(
            TRANSPORT_DOCUMENT_SUMMARIES
                + "?carrierBookingReference="
                + carrierBookingReference
                + "&documentStatus="
                + documentStatus)
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_OK)
        .body(jsonSchemaValidator("transportDocumentSummary"));
    }

  ShippingInstructionResponseTO createShippingInstruction(Map<String, Object> map) {

    return given()
        .contentType("application/json")
        .body(map)
        .post(SHIPPING_INSTRUCTIONS)
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_CREATED)
        .extract()
        .response()
        .as(ShippingInstructionResponseTO.class);
  }
}
