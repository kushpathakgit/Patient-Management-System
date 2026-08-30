package com.pm.apigateway.config;

import com.pm.apigateway.filter.JwtValidationGatewayFilterFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder,
                                      @Value("${auth.service.url:http://localhost:4005}") String authServiceUrl,
                                      @Value("${patient.service.url:http://localhost:4000}") String patientServiceUrl,
                                      JwtValidationGatewayFilterFactory jwtValidationGatewayFilterFactory) {
        return builder.routes()
                .route("auth-service-route", route -> route
                        .path("/auth/**")
                        .filters(filter -> filter.stripPrefix(1))
                        .uri(authServiceUrl))
                .route("patient-service-route", route -> route
                        .path("/api/patients/**")
                        .filters(filter -> filter
                                .stripPrefix(1)
                                .filter(jwtValidationGatewayFilterFactory.apply(new Object())))
                        .uri(patientServiceUrl))
                .route("api-docs-patient-route", route -> route
                        .path("/api-docs/patients")
                        .filters(filter -> filter.rewritePath("/api-docs/patients", "/v3/api-docs"))
                        .uri(patientServiceUrl))
                .route("api-docs-auth-route", route -> route
                        .path("/api-docs/auth")
                        .filters(filter -> filter.rewritePath("/api-docs/auth", "/v3/api-docs"))
                        .uri(authServiceUrl))
                .build();
    }
}
