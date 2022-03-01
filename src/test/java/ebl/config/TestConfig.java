package ebl.config;

import io.restassured.RestAssured;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class TestConfig {

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
}
