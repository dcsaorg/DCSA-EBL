package ebl.config;

import com.github.fge.jsonschema.SchemaVersion;
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

  // API endpoints
  public static final String SHIPPING_INSTRUCTIONS = "/v2/shipping-instructions";
  public static final String TRANSPORT_DOCUMENTS = "/v2/transport-documents";
  public static final String TRANSPORT_DOCUMENT_SUMMARIES = "/v2/transport-document-summaries";

  private TestConfig() {}

  public static void init() throws IOException {
    File file = new File("src/test/resources/test.properties");
    try (FileInputStream fis = new FileInputStream(file)) {
      Properties properties = new Properties();
      properties.load(fis);
      RestAssured.baseURI = properties.getProperty("base_url");
      RestAssured.port = Integer.parseInt(properties.getProperty("port"));
    }
  }

  public static Matcher<?> jsonSchemaValidator(String filename) {
    if (!filename.endsWith(".json")) filename += ".json";
    return matchesJsonSchemaInClasspath("schema/" + filename)
        .using(
            JsonSchemaFactory.newBuilder()
                .setValidationConfiguration(
                    ValidationConfiguration.newBuilder().setDefaultVersion(DRAFTV3).freeze())
                .freeze());
  }

  public static Matcher<?> jsonSchemaValidator(String fileName, SchemaVersion schemaVersion) {
    return matchesJsonSchemaInClasspath("schema/" + fileName + ".json")
      .using(
        JsonSchemaFactory.newBuilder()
          .setValidationConfiguration(
            ValidationConfiguration.newBuilder().setDefaultVersion(schemaVersion).freeze())
          .freeze());
  }
}
