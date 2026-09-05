package com.tabletennis.app.config;
import org.springframework.context.annotation.*;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.*;
@Configuration
public class SwaggerConfig {
    @Bean OpenAPI openAPI() { return new OpenAPI().info(new Info().title("탁구 경기 관리 API").version("v1"))
        .components(new Components().addSecuritySchemes("bearerAuth",new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
        .addSecurityItem(new SecurityRequirement().addList("bearerAuth")); }
}
