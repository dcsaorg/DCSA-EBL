package ebl.v2;

import ebl.config.TestConfig;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
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
  void shippingInstructionWithOneCarrierBookingReference() {
    given()
      .contentType("application/json")
      .queryParam("carrierBookingReference", "02c965382f5a41feb9f19b24b5fe2906")
      .get(SUMMARIES_ENDPOINT)
      .then()
      .assertThat()
      .statusCode(HttpStatus.SC_OK)
      .body("size()", is(1))
      .body("[0].shippingInstructionReference", equalTo("cb6354c9-1ceb-452c-aed0-3cb25a04647a"))
      .body("[0].documentStatus", equalTo("PENU"))
      .body("[0].carrierBookingReferences.size()", is(1))
      .body("[0].carrierBookingReferences", containsInAnyOrder(equalTo("02c965382f5a41feb9f19b24b5fe2906")))
      .extract()
      .body()
      .asString();
  }

  @Test
  void shippingInstructionWithTwoCarrierBookingReferences() {
    given()
      .contentType("application/json")
      .queryParam("carrierBookingReference", "bca68f1d3b804ff88aaa1e43055432f7")
      .get(SUMMARIES_ENDPOINT)
      .then()
      .assertThat()
      .statusCode(HttpStatus.SC_OK)
      .body("size()", is(1))
      .body("[0].shippingInstructionReference", equalTo("9d5965a5-9e2f-4c78-b8cb-fbb7095e13a0"))
      .body("[0].documentStatus", equalTo("APPR"))
      .body("[0].carrierBookingReferences.size()", is(2))
      .body("[0].carrierBookingReferences", containsInAnyOrder(equalTo("bca68f1d3b804ff88aaa1e43055432f7"),equalTo("832deb4bd4ea4b728430b857c59bd057")))
      .extract()
      .body()
      .asString();
  }

  @Test
  void filterByDocumentStatus() {
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
  void combineCarrierBookingReferencesAndDocumentStatusNoMatch() {
    given()
        .contentType("application/json")
        .queryParam("carrierBookingReference", "bca68f1d3b804ff88aaa1e43055432f7")
        .queryParam("documentStatus", "RECE")
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
  void combineCarrierBookingReferencesAndDocumentStatusOneMatch() {
    given()
      .contentType("application/json")
      .queryParam("carrierBookingReference", "02c965382f5a41feb9f19b24b5fe2906")
      .queryParam("documentStatus", "PENU")
      .get(SUMMARIES_ENDPOINT)
      .then()
      .assertThat()
      .statusCode(HttpStatus.SC_OK)
      .body("size()", is(1))
      .body("[0].shippingInstructionReference", equalTo("cb6354c9-1ceb-452c-aed0-3cb25a04647a"))
      .body("[0].documentStatus", equalTo("PENU"))
      .body("[0].carrierBookingReferences.size()", is(1))
      .body("[0].carrierBookingReferences", containsInAnyOrder(equalTo("02c965382f5a41feb9f19b24b5fe2906")))
      .extract()
      .body()
      .asString();
  }
}
