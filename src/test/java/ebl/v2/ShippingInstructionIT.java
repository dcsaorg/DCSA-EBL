package ebl.v2;

import ebl.config.TestConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.UUID;

import static io.restassured.RestAssured.given;

class ShippingInstructionIT {

  @BeforeAll
  void configs() throws IOException {
    TestConfig.init();
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
