package ebl.config;

import com.github.fge.jsonschema.cfg.ValidationConfiguration;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import io.restassured.RestAssured;
import org.hamcrest.Matcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static com.github.fge.jsonschema.SchemaVersion.DRAFTV3;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

public class TestConfig {

  public static final String ROOT_URI =
      System.getenv().getOrDefault("API_ROOT_URI", "http://localhost");
  private static final String PORT = System.getenv().getOrDefault("PORT", "9090");

  // API endpoints
  public static final String SHIPPING_INSTRUCTIONS = "/v2/shipping-instructions";

  private TestConfig() {}

  public static String baseURL() {
    return ROOT_URI + ":" + PORT;
  }

  public static void init() throws IOException {
    File file = new File("src/test/resources/test.properties");
    try (FileInputStream fis = new FileInputStream(file)) {
      Properties properties = new Properties();
      properties.load(fis);
      RestAssured.baseURI = properties.getProperty("base_url");
      RestAssured.port = Integer.parseInt(properties.getProperty("port"));
    }
  }

  public static Matcher<?> jsonSchemaValidator(String fileName) {
    return matchesJsonSchemaInClasspath("schema/" + fileName + ".json")
        .using(
            JsonSchemaFactory.newBuilder()
                .setValidationConfiguration(
                    ValidationConfiguration.newBuilder().setDefaultVersion(DRAFTV3).freeze())
                .freeze());
  }
}
