package ebl.v2;

import ebl.config.TestConfig;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.github.fge.jsonschema.SchemaVersion.DRAFTV4;
import static ebl.config.TestConfig.*;
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
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(0))
            .body("transportDocumentReference", anyOf(hasItem("9b02401c-b2fb-5009")));
  }

  @Test
  void testValidGetTransportDocument(){

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
  void testGetNotFoundTransportDocument(){

    given()
        .contentType("application/json")
        .get(TRANSPORT_DOCUMENTS + "/" + "DoesNotExists")
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_NOT_FOUND)
        .body("httpMethod", equalTo("GET"))
        .body("requestUri", containsString("/v2/transport-documents/"))
        .body("errors[0].reason", equalTo("notFound"))
        .body("errors[0].message", containsString("No transport document found with transport document reference"))
        .body("statusCode", equalTo(404))
        .body("statusCodeText", equalTo("Not Found"))
        .extract()
        .body()
        .asString();
  }
}
