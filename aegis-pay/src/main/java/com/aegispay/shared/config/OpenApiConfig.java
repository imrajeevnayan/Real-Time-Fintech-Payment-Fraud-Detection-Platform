package com.aegispay.shared.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(
        title = "Aegis Pay API",
        version = "v1",
        description = "Real-time fraud detection & auto-remediation platform"))
public class OpenApiConfig {
}
