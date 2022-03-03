package org.dcsa.ebl.controller;

import com.github.fge.jsonschema.cfg.ValidationConfiguration;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import org.hamcrest.Matcher;
import org.springframework.test.web.reactive.server.EntityExchangeResult;

import java.nio.charset.StandardCharsets;

import static com.github.fge.jsonschema.SchemaVersion.DRAFTV4;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;

public class JsonSchemaValidator {

  private static final String SCHEMA_BASE_PATH = "schema/";

  public static void validateAgainstJsonSchema(EntityExchangeResult<byte[]> response, String schemaPath) {
    String responseJson = new String(response.getResponseBody(), StandardCharsets.UTF_8);

    Matcher matcher = matchesJsonSchemaInClasspath(SCHEMA_BASE_PATH + schemaPath).using(
      JsonSchemaFactory.newBuilder()
        .setValidationConfiguration(
          ValidationConfiguration.newBuilder().setDefaultVersion(DRAFTV4).freeze())
        .freeze());

    assertThat(responseJson, matcher);
  }
}
