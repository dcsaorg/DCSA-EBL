package org.dcsa.ebl.controller;

import com.github.fge.jsonschema.cfg.ValidationConfiguration;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import org.springframework.test.web.reactive.server.EntityExchangeResult;

import java.nio.charset.StandardCharsets;

import static com.github.fge.jsonschema.SchemaVersion.DRAFTV3;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JsonSchemaValidator {

  public static void validateAgainstJsonSchema(EntityExchangeResult<byte[]> response) {
    String responseJson = new String(response.getResponseBody(), StandardCharsets.UTF_8);
    assertTrue( matchesJsonSchemaInClasspath("schemas/all.json").using(
      JsonSchemaFactory.newBuilder()
        .setValidationConfiguration(
          ValidationConfiguration.newBuilder().setDefaultVersion(DRAFTV3).freeze())
        .freeze()).matches(responseJson));
  }
}
