package org.dcsa.ebl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dcsa.core.repository.ExtendedRepositoryImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@SpringBootApplication
public class Application {

	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper()
				.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
				.enable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES)
				.findAndRegisterModules();
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
