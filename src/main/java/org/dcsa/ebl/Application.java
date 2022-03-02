package org.dcsa.ebl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.text.SimpleDateFormat;

@SpringBootApplication
public class Application {

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper()
        .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .enable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES)
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
        .setDateFormat(new SimpleDateFormat("yyyy-MM-dd"))
        .findAndRegisterModules();
  }

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
