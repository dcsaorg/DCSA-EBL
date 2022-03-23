package ebl.v2;

import com.fasterxml.jackson.databind.ObjectMapper;
import ebl.config.TestConfig;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.core.events.model.transferobjects.ShippingInstructionTO;
import org.dcsa.ebl.Application;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static ebl.config.TestConfig.SHIPPING_INSTRUCTIONS;
import static ebl.config.TestConfig.jsonSchemaValidator;
import static ebl.config.TestUtil.loadFileAsString;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class ShippingInstructionPutIT {
  // ObjectMapper with the same config as the main app
  private final ObjectMapper objectMapper = new Application().objectMapper();

  @BeforeAll
  static void configs() throws IOException {
    TestConfig.init();
  }

  @Test
  void acceptChangeToDRFT() throws IOException {
    ShippingInstructionTO shippingInstruction = createShippingInstruction(false);

    given()
      .contentType("application/json")
      .body(objectMapper.writeValueAsString(shippingInstruction))
      .put(SHIPPING_INSTRUCTIONS + "/" + shippingInstruction.getShippingInstructionReference())
      .then()
      .assertThat()
      .statusCode(HttpStatus.SC_OK)
      .body("shippingInstructionReference", equalTo(shippingInstruction.getShippingInstructionReference()))
      .body("documentStatus", equalTo("APPR"))
      .body(jsonSchemaValidator("shippingInstructionResponse"))
      .extract()
      .body()
      .asString();
  }

  @Test
  void noChange() throws IOException {
    ShippingInstructionTO shippingInstruction = createShippingInstruction(true);

    given()
        .contentType("application/json")
        .body(objectMapper.writeValueAsString(shippingInstruction))
        .put(SHIPPING_INSTRUCTIONS + "/" + shippingInstruction.getShippingInstructionReference())
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_OK)
        .body("shippingInstructionReference", equalTo(shippingInstruction.getShippingInstructionReference()))
        .body("documentStatus", equalTo("PENU"))
        .body(jsonSchemaValidator("shippingInstructionResponse"))
        .extract()
        .body()
        .asString();
  }

  @Test
  void changeElectronicAndNumberOfCopies() throws IOException {
    ShippingInstructionTO shippingInstruction = createShippingInstruction(true);
    shippingInstruction.setIsElectronic(true);
    shippingInstruction.setNumberOfCopies(5);

    given()
        .contentType("application/json")
        .body(objectMapper.writeValueAsString(shippingInstruction))
        .put(SHIPPING_INSTRUCTIONS + "/" + shippingInstruction.getShippingInstructionReference())
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_OK)
        .body(
            "shippingInstructionReference",
            equalTo(shippingInstruction.getShippingInstructionReference()))
        .body("documentStatus", equalTo("DRFT"))
        .body(jsonSchemaValidator("shippingInstructionResponse"))
        .extract()
        .body()
        .asString();
  }

  @Test
  void failWrongDocumentStatus() throws IOException {
    String siReference = "8fbb78cc-e7c6-4e17-9a23-24dc3ad0378d";
    ShippingInstructionTO shippingInstruction = objectMapper.readValue(loadFileAsString("ValidShippingInstruction.json"), ShippingInstructionTO.class);
    shippingInstruction.setShippingInstructionReference(siReference);

    assert shippingInstruction.getShippingInstructionReference() != null;

    given()
      .contentType("application/json")
      .body(objectMapper.writeValueAsString(shippingInstruction))
      .put(SHIPPING_INSTRUCTIONS + "/" + siReference)
      .then()
      .assertThat()
      .statusCode(HttpStatus.SC_BAD_REQUEST)
      .body("httpMethod", equalTo("PUT"))
      .body("requestUri", containsString(SHIPPING_INSTRUCTIONS + "/" + shippingInstruction.getShippingInstructionReference()))
      .body("errors[0].reason", equalTo("invalidParameter"))
      .body("errors[0].message", containsString("DocumentStatus needs to be set to PENU or DRFT"))
      .body("statusCode", equalTo(HttpStatus.SC_BAD_REQUEST))
      .body("statusCodeText", equalTo("Bad Request"))
      // .body(jsonSchemaValidator("error")) // invalid JSON Schema
      .extract()
      .body()
      .asString();
  }

  @Test
  void failWithUnknownBooking() throws IOException {
    ShippingInstructionTO shippingInstruction = createShippingInstruction(true);
    shippingInstruction.setCarrierBookingReference("not_exists");
    shippingInstruction.setNumberOfCopies(5);

    given()
        .contentType("application/json")
        .body(objectMapper.writeValueAsString(shippingInstruction))
        .put(SHIPPING_INSTRUCTIONS + "/" + shippingInstruction.getShippingInstructionReference())
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_NOT_FOUND)
        .body("httpMethod", equalTo("PUT"))
        .body(
            "requestUri",
            containsString(
                SHIPPING_INSTRUCTIONS
                    + "/"
                    + shippingInstruction.getShippingInstructionReference()))
        .body("errors[0].reason", equalTo("notFound"))
        .body("statusCode", equalTo(HttpStatus.SC_NOT_FOUND))
        .body("statusCodeText", equalTo("Not Found"))
        // .body(jsonSchemaValidator("error")) // invalid JSON Schema
        .extract()
        .body()
        .asString();
  }

  @Test
  void failWithBookingWrongState() throws IOException {
    ShippingInstructionTO shippingInstruction = createShippingInstruction(true);
    shippingInstruction.setCarrierBookingReference("BR1239719971");
    shippingInstruction.setNumberOfCopies(5);

    given()
        .contentType("application/json")
        .body(objectMapper.writeValueAsString(shippingInstruction))
        .put(SHIPPING_INSTRUCTIONS + "/" + shippingInstruction.getShippingInstructionReference())
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .body("httpMethod", equalTo("PUT"))
        .body(
            "requestUri",
            containsString(
                SHIPPING_INSTRUCTIONS
                    + "/"
                    + shippingInstruction.getShippingInstructionReference()))
        .body("errors[0].reason", equalTo("invalidParameter"))
        .body("errors[0].message", containsString("is not in CONF state"))
        .body("statusCode", equalTo(HttpStatus.SC_BAD_REQUEST))
        .body("statusCodeText", equalTo("Bad Request"))
        // .body(jsonSchemaValidator("error")) // invalid JSON Schema
        .extract()
        .body()
        .asString();
  }

  /**
   * Create a ShippingInstruction that can be manipulated.
   */
  private ShippingInstructionTO createShippingInstruction(boolean withMinorErrors) throws IOException {
    ShippingInstructionTO shippingInstruction = objectMapper.readValue(loadFileAsString("ValidShippingInstruction.json"), ShippingInstructionTO.class);

    if (withMinorErrors) {
      shippingInstruction.setDocumentStatus(null);
      shippingInstruction.setIsElectronic(false);
      shippingInstruction.setNumberOfCopies(null);
    } else {
      shippingInstruction.setDocumentStatus(null);
      shippingInstruction.setIsElectronic(true);
      shippingInstruction.setNumberOfCopies(7);
    }

    Response response =
        given()
            .contentType("application/json")
            .body(objectMapper.writeValueAsString(shippingInstruction))
            .post(SHIPPING_INSTRUCTIONS);
    String reference = response.body().jsonPath().getString("shippingInstructionReference");

    System.out.println("Reference: " + reference);
    assert response.statusCode() == HttpStatus.SC_CREATED;
    assert reference != null;

    shippingInstruction.setShippingInstructionReference(reference);
    return shippingInstruction;
  }
}
