package com.tabletennis.app.config;
import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.cors.*;
import java.util.*;
@Configuration
public class CorsConfig {
    @Bean CorsConfigurationSource corsConfigurationSource(@Value("${cors.allowed-origins}") String origins) {
        CorsConfiguration c=new CorsConfiguration(); c.setAllowedOrigins(Arrays.stream(origins.split(",")).map(String::trim).toList());
        c.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS")); c.setAllowedHeaders(List.of("Authorization","Content-Type"));
        UrlBasedCorsConfigurationSource source=new UrlBasedCorsConfigurationSource(); source.registerCorsConfiguration("/**",c); return source;
    }
}
