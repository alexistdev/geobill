package com.alexistdev.geobill.utils;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ObjectConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
//        return new WebMvcConfigurer() {
//            @Override
//            public void addCorsMappings(CorsRegistry registry) {
//                registry.addMapping("/**")
//                        .allowedOrigins("http://localhost:4200")  // Specify your Angular app's URL
//                        .allowedMethods(
//                                HttpMethod.POST.name(),
//                                HttpMethod.GET.name(),
//                                HttpMethod.DELETE.name(),
//                                HttpMethod.PUT.name(),
//                                HttpMethod.PATCH.name(),
//                                HttpMethod.OPTIONS.name()  // Important for preflight requests
//                        )
//                        .allowedHeaders("*")  // Allow all headers
//                        .allowCredentials(true)  // Allow credentials
//                        .maxAge(3600L);  // Cache preflight response for 1 hour
//            }
//        };
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/v1/**")
                        .allowedOrigins("http://localhost:4200") // Angular dev server
                        .allowedHeaders("Authorization", "Content-Type")
                        .allowedMethods("GET", "POST", "PATCH", "DELETE", "OPTIONS")
                        .allowCredentials(true);
            }
        };

    }

}
