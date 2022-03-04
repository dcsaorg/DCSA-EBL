package ebl.v2;

import ebl.config.TestConfig;
import io.restassured.http.ContentType;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class ShipmentEventIT {

  @BeforeAll
  static void configs() throws IOException {
    TestConfig.init();
  }

  @Test
  void testGetAllEvents() {
    given()
      .contentType("application/json")
      .get("/v2/events")
      .then()
      .assertThat()
      .statusCode(200)
      .contentType(ContentType.JSON)
      .body("size()", greaterThanOrEqualTo(0))
      .body("eventType", everyItem(equalTo("SHIPMENT")))
      ;
  }

  @Test
  void testGetAllEventsByShipmentEventTypeCode() {
    BiConsumer<String, Matcher<String>> runner = (s, m) ->
        given()
        .contentType("application/json")
        .queryParam("shipmentEventTypeCode", s)
        .get("/v2/events")
        .then()
        .assertThat()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("size()", greaterThanOrEqualTo(0))
        .body("eventType", everyItem(equalTo("SHIPMENT")))
        .body("shipmentEventTypeCode", everyItem(m))
    ;

    runner.accept("APPR,ISSU", anyOf(equalTo("APPR"), equalTo("ISSU")));
    runner.accept("APPR", equalTo("APPR"));
    runner.accept("ISSU", equalTo("ISSU"));
  }

  @Test
  void testGetAllEventsByDocumentTypeCodeCode() {
    BiConsumer<String, Matcher<String>> runner = (s, m) ->
      given()
        .contentType("application/json")
        .queryParam("documentTypeCode", s)
        .get("/v2/events")
        .then()
        .assertThat()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("size()", greaterThanOrEqualTo(0))
        .body("eventType", everyItem(equalTo("SHIPMENT")))
        .body("documentTypeCode", everyItem(m))
      ;
    runner.accept("SHI,TRD", anyOf(equalTo("SHI"), equalTo("TRD")));
    runner.accept("SHI", equalTo("SHI"));
    runner.accept("TRD", equalTo("TRD"));
  }

  @Test
  void testGetAllEventsByCombinedQuery() {
    given()
      .contentType("application/json")
      // VOID only applies to TRD, so this is guaranteed to give 0 matches.
      .queryParam("documentTypeCode", "SHI")
      .queryParam("shipmentEventTypeCode", "VOID")
      .get("/v2/events")
      .then()
      .assertThat()
      .statusCode(200)
      .contentType(ContentType.JSON)
      .body("size()", equalTo(0))
    ;
  }


}
