package ebl.v2;

import ebl.config.TestConfig;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;

public class ShippingInstructionSummariesIT {
  private static final String SUMMARIES_ENDPOINT = "/v2/shipping-instructions-summaries";

  @BeforeAll
  static void configs() throws IOException {
    TestConfig.init();
  }

  @Test
  void allShippingInstructions() {
    given()
      .contentType("application/json")
      .get(SUMMARIES_ENDPOINT)
      .then()
      .assertThat()
      .statusCode(HttpStatus.SC_OK)
      .body("size()", greaterThanOrEqualTo(5)) // We know that the test data set contains at least 5 shipping instructions
      .extract()
      .body()
      .asString();
  }

  @Test
  void noShippingInstructions() {
    given()
      .contentType("application/json")
      .queryParam("carrierBookingReference", "do_not_exist")
      .get(SUMMARIES_ENDPOINT)
      .then()
      .assertThat()
      .statusCode(HttpStatus.SC_OK)
      .body("size()", is(0))
      .extract()
      .body()
      .asString();
  }

  @Test
  void filterByDocumentStatusNoError() {
    Response allResponse =
      given()
        .contentType("application/json")
        .get(SUMMARIES_ENDPOINT);
    int allCount = allResponse.body().jsonPath().getList("$").size();

    given()
      .contentType("application/json")
      .queryParam("documentStatus", "RECE")
      .get(SUMMARIES_ENDPOINT)
      .then()
      .assertThat()
      .statusCode(HttpStatus.SC_OK)
      .body("size()", greaterThanOrEqualTo(3)) // We know that the test data set contains at least 3 shipping instructions with RECE
      .body("size()", lessThan(allCount))
      .extract()
      .body()
      .asString();
  }

  @Test
  void combineCarrierBookingReferencesAndDocumentStatusNoError() {
    given()
        .contentType("application/json")
        .queryParam("carrierBookingReference", "bca68f1d3b804ff88aaa1e43055432f7")
        .queryParam("documentStatus", "RECE")
        .get(SUMMARIES_ENDPOINT)
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_OK)
        .extract()
        .body()
        .asString();
  }
}
