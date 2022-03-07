package ebl.v2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ebl.config.TestConfig;
import org.apache.http.HttpStatus;
import org.dcsa.core.events.model.transferobjects.ShippingInstructionTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import static ebl.config.TestConfig.*;
import static ebl.config.TestUtil.jsonToMap;
import static ebl.config.TestUtil.loadFileAsString;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class ShippingInstructionIT {

  private final String VALID_SHIPPING_INSTRUCTION =
      loadFileAsString("ValidShippingInstruction.json");

  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeAll
  static void configs() throws IOException {
    TestConfig.init();
  }

  @Test
  void testValidPostShippingInstruction() {
    given()
        .contentType("application/json")
        .body(VALID_SHIPPING_INSTRUCTION)
        .post(SHIPPING_INSTRUCTIONS)
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_CREATED)
        .body("shippingInstructionReference", notNullValue())
        .body("documentStatus", equalTo("PENC"))
        .body("shippingInstructionCreatedDateTime", notNullValue())
        .body("shippingInstructionUpdatedDateTime", notNullValue())
        .body(jsonSchemaValidator("shippingInstructionRequest"))
        .extract()
        .body()
        .asString();
  }

  @Test
  void testFailureOnEmptyBodyForPostShippingInstruction() throws JsonProcessingException {

    ShippingInstructionTO invalid_shipping_instruction = new ShippingInstructionTO();

    given()
        .contentType("application/json")
        .body(objectMapper.writeValueAsString(invalid_shipping_instruction))
        .post(SHIPPING_INSTRUCTIONS)
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .body("httpMethod", equalTo("POST"))
        .body("requestUri", containsString("/v2/shipping-instructions"))
        .body("errors[0].reason", equalTo("invalidInput"))
        .body("errors[0].message", containsString("isToOrder"))
        .body("errors[0].message", containsString("shipmentEquipments"))
        .body("errors[0].message", containsString("isShippedOnboardType"))
        .body("statusCode", equalTo(400))
        .body("statusCodeText", equalTo("Bad Request"))
        .extract()
        .body()
        .asString();
  }

  @Test
  void testFailureOnNonExistingCarrBookingRefPostShippingInstruction() {

    Map<String, Object> map = jsonToMap(VALID_SHIPPING_INSTRUCTION);
    assert map != null;
    map.put("carrierBookingReference", UUID.randomUUID().toString());

    given()
        .contentType("application/json")
        .body(map)
        .post(SHIPPING_INSTRUCTIONS)
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .body("httpMethod", equalTo("POST"))
        .body("requestUri", containsString("/v2/shipping-instructions"))
        .body("errors[0].reason", equalTo("invalidParameter"))
        .body("errors[0].message", containsString("No shipment found with carrierBookingReference"))
        .body("statusCode", equalTo(400))
        .body("statusCodeText", equalTo("Bad Request"))
        .extract()
        .body()
        .asString();
  }
}
