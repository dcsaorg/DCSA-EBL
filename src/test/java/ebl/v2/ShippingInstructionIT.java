package ebl.v2;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;

class ShippingInstructionIT {

  @BeforeEach
  void init() {
    RestAssured.baseURI = "http://localhost";
    RestAssured.port = 9090;
  }

  // dummy test to verify flow integration works
  @Test
  void test() {
    given()
        .contentType("application/json")
        .get("/v2/shipping-instructions/" + UUID.randomUUID())
        .then()
        .assertThat()
        .statusCode(200);
  }
}
