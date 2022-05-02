package ebl.v2;

import ebl.config.TestConfig;
import io.restassured.http.ContentType;
import lombok.RequiredArgsConstructor;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.function.BiConsumer;

import static ebl.config.TestConfig.jsonSchemaValidator;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.matchesRegex;

class ShipmentEventIT {

  @BeforeAll
  static void configs() throws IOException {
    TestConfig.init();
  }

  @Test
  void testGetAllEventsAndHeaders() {
    given()
      .contentType("application/json")
      .get("/v2/events")
      .then()
      .assertThat()
      .statusCode(200)
      .contentType(ContentType.JSON)
      .header("API-Version", equalTo("2.0.0"))
      .header("Current-Page", matchesRegex("^https?://.*/v2/events\\?cursor=[a-zA-Z\\d]*$"))
      .header("Next-Page", matchesRegex("^https?://.*/v2/events\\?cursor=[a-zA-Z\\d]*$"))
      .header("Last-Page", matchesRegex("^https?://.*/v2/events\\?cursor=[a-zA-Z\\d]*$"))
      .body("size()", greaterThanOrEqualTo(0))
      .body("eventType", everyItem(equalTo("SHIPMENT")))
      .body("eventClassifierCode", everyItem(equalTo("ACT")))
      .body(jsonSchemaValidator("shipmentEvent"));
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
        .body("eventClassifierCode", everyItem(equalTo("ACT")))
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
        .body("eventClassifierCode", everyItem(equalTo("ACT")))
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

  @Test
  void testGetAllEventsByCarrierBookingReference() {
    given()
      .contentType("application/json")
      .queryParam("carrierBookingReference", "832deb4bd4ea4b728430b857c59bd057")
      .get("/v2/events")
      .then()
      .assertThat()
      .statusCode(200)
      .contentType(ContentType.JSON)
      // The test data includes at least 3 shipment events related to the reference. But something adding additional
      // events.
      .body("size()", greaterThanOrEqualTo(3))
      .body("eventType", everyItem(equalTo("SHIPMENT")))
      .body("eventClassifierCode", everyItem(equalTo("ACT")))
      .body("documentTypeCode", everyItem(anyOf(equalTo("SHI"), equalTo("TRD"))))
      .body("documentReferences.flatten().findAll { it.documentReferenceType == 'BKG' }.size()", greaterThanOrEqualTo(3))
      .body("documentReferences.flatten().findAll { it.documentReferenceType == 'BKG' }.documentReferenceValue", everyItem(equalTo("832deb4bd4ea4b728430b857c59bd057")))
    ;
  }

  @Test
  void testGetAllEventsByCarrierBookingRequestReference() {
    given()
      .contentType("application/json")
      .queryParam("carrierBookingRequestReference", "CARRIER_BOOKING_REQUEST_REFERENCE_01")
      .get("/v2/events")
      .then()
      .assertThat()
      .statusCode(200)
      .contentType(ContentType.JSON)
      // The test data includes at least 3 shipment events related to the reference. But something adding additional
      // events.
      .body("size()", greaterThanOrEqualTo(3))
      .body("eventType", everyItem(equalTo("SHIPMENT")))
      .body("eventClassifierCode", everyItem(equalTo("ACT")))
      .body("documentTypeCode", everyItem(anyOf(equalTo("SHI"), equalTo("TRD"))))
      .body("documentReferences.flatten().findAll { it.documentReferenceType == 'CBR' }.size()", greaterThanOrEqualTo(3))
      .body("documentReferences.flatten().findAll { it.documentReferenceType == 'CBR' }.documentReferenceValue", everyItem(equalTo("CARRIER_BOOKING_REQUEST_REFERENCE_01")))
    ;
  }

  @Test
  void testGetAllEventsByTransportDocumentReference() {
    given()
      .contentType("application/json")
      .queryParam("transportDocumentReference", "2b02401c-b2fb-5009")
      .get("/v2/events")
      .then()
      .assertThat()
      .statusCode(200)
      .contentType(ContentType.JSON)
      // The test data includes at least 3 shipment events related to the reference. But something adding additional
      // events.
      .body("size()", greaterThanOrEqualTo(3))
      .body("eventType", everyItem(equalTo("SHIPMENT")))
      .body("eventClassifierCode", everyItem(equalTo("ACT")))
      .body("documentTypeCode", everyItem(anyOf(equalTo("SHI"), equalTo("TRD"))))
      .body("documentReferences.flatten().findAll { it.documentReferenceType == 'TRD' }.size()", greaterThanOrEqualTo(3))
      .body("documentReferences.flatten().findAll { it.documentReferenceType == 'TRD' }.documentReferenceValue", everyItem(equalTo("2b02401c-b2fb-5009")))
    ;
  }

  @Test
  void testGetAllEventsByCarrierBookingReferenceWithEventCreatedDateTimeRange() {
    String rangeStart = "2021-01-08T00:00:00Z";
    String rangeEnd = "2021-01-09T00:00:00Z";
    given()
      .contentType("application/json")
      .queryParam("carrierBookingReference", "832deb4bd4ea4b728430b857c59bd057")
      .queryParam("eventCreatedDateTime:gte", rangeStart)
      .queryParam("eventCreatedDateTime:lt", rangeEnd)
      .get("/v2/events")
      .then()
      .assertThat()
      .statusCode(200)
      .contentType(ContentType.JSON)
      // The test data includes 3 shipment events for this case. Given the narrow date range, it seems acceptable to
      // validate an exact match.
      .body("size()", equalTo(3))
      .body("eventType", everyItem(equalTo("SHIPMENT")))
      .body("eventClassifierCode", everyItem(equalTo("ACT")))
      .body("documentTypeCode", everyItem(anyOf(equalTo("SHI"), equalTo("TRD"))))
      .body("eventCreatedDateTime", everyItem(
        asDateTime(
          allOf(
            greaterThanOrEqualTo(ZonedDateTime.parse(rangeStart)),
            lessThan(ZonedDateTime.parse(rangeEnd))
      ))))
      .body("documentReferences.flatten().findAll { it.documentReferenceType == 'BKG' }.size()", greaterThanOrEqualTo(3))
      .body("documentReferences.flatten().findAll { it.documentReferenceType == 'BKG' }.documentReferenceValue", everyItem(equalTo("832deb4bd4ea4b728430b857c59bd057")))
    ;
  }

  @Test
  void testGetAllEventsByEventCreatedDateTimeRange() {
    String rangeStart = "2021-01-08T00:00:00Z";
    // 10:00-0400 is 14:00 at Z, so the first event for CBR 832deb4bd4ea4b728430b857c59bd057 is included while the
    // latter to are excluded
    String rangeEnd = "2021-01-08T10:00:00-04:00";
    given()
      .contentType("application/json")
      .queryParam("eventCreatedDateTime:gte", rangeStart)
      .queryParam("eventCreatedDateTime:lt", rangeEnd)
      .get("/v2/events")
      .then()
      .assertThat()
      .statusCode(200)
      .contentType(ContentType.JSON)
      // The test data includes 1 shipment event for this case. Given the narrow date range, it seems acceptable to
      // validate an exact match.  Note the strict match is used to validate that the TZ conversion works correctly
      // when filtering
      .body("size()", equalTo(1))
      .body("eventType", everyItem(equalTo("SHIPMENT")))
      .body("eventClassifierCode", everyItem(equalTo("ACT")))
      .body("documentTypeCode", everyItem(anyOf(equalTo("SHI"), equalTo("TRD"))))
      .body("eventCreatedDateTime", everyItem(
        asDateTime(
          allOf(
            greaterThanOrEqualTo(ZonedDateTime.parse(rangeStart)),
            lessThan(ZonedDateTime.parse(rangeEnd))
      ))))
      .body("documentReferences.flatten().findAll { it.documentReferenceType == 'BKG' }.size()", greaterThanOrEqualTo(1))
      .body("documentReferences.flatten().findAll { it.documentReferenceType == 'BKG' }.documentReferenceValue", everyItem(equalTo("832deb4bd4ea4b728430b857c59bd057")))
    ;
  }

  /**
   * Convert the input (assumed to be String) into a ZonedDateTime before chaining off to the next
   * match
   *
   * <p>The conversion will use {@link ZonedDateTime#parse(CharSequence)}. If the parsing fails, the
   * value is assumed not to match.
   *
   * @param dateTimeMatcher The matcher that should operator on a ZonedDateTime
   * @return The combined matcher
   */
  // Use ChronoZonedDateTime as bound to avoid fighting generics with lessThan that "reduces"
  // ZonedDateTime
  // to the ChronoZonedDateTime (via Comparable)
  private static <T extends ChronoZonedDateTime<?>> Matcher<T> asDateTime(
      Matcher<T> dateTimeMatcher) {
    return new DateTimeMatcher<>(dateTimeMatcher);
  }

  @RequiredArgsConstructor
  private static class DateTimeMatcher<T extends ChronoZonedDateTime<?>> extends BaseMatcher<T> {

    private final Matcher<T> matcher;

    @Override
    public boolean matches(Object actual) {
      ZonedDateTime dateTime;
      if (!(actual instanceof String)) {
        return false;
      }
      try {
        dateTime = ZonedDateTime.parse((String) actual);
      } catch (DateTimeParseException e) {
        return false;
      }
      return matcher.matches(dateTime);
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("as datetime ").appendDescriptionOf(matcher);
    }
  }
}
