package org.dcsa.ebl.controller;

import org.dcsa.core.exception.handler.GlobalExceptionHandler;
import org.dcsa.core.security.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DisplayName("Tests for ShippingInstructionSummariesController")
@ActiveProfiles("test")
@WebFluxTest(controllers = {ShippingInstructionSummariesController.class})
@Import(value = {GlobalExceptionHandler.class, SecurityConfig.class})
public class ShippingInstructionSummariesControllerTest {
}
