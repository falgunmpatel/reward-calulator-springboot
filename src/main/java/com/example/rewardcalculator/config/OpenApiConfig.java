package com.example.rewardcalculator.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI rewardCalculatorOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Retailer Rewards Calculator API")
                        .description("""
                                REST API for calculating customer reward points based on purchase transactions.

                                **Points Calculation Rules:**
                                - $0 - $50: 0 points
                                - $50.01 - $100: 1 point per dollar over $50
                                - Over $100: 2 points per dollar over $100 + 50 points for the $50-$100 tier

                                **Example:** A $120 purchase earns (20 × 2) + 50 = 90 points.

                                **Features:**
                                - Pagination support (page, size parameters)
                                - Date range filtering (from, to parameters in ISO-8601 format)
                                - Comprehensive error handling with structured responses
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Falgun Patel")
                                .email("falgunpatel9123@gmail.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8081")
                                .description("Development Server")
                ));
    }
}

