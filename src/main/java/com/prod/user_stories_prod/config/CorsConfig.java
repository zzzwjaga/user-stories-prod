package com.prod.user_stories_prod.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class CorsConfig {

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
      CorsConfiguration corsConfiguration = new CorsConfiguration();

      corsConfiguration.setAllowedOrigins(List.of(
              "http://127.0.0.1:5500",
              "http://localhost:5500"
      ));

      corsConfiguration.setAllowedMethods(List.of("*"));
      corsConfiguration.setAllowedHeaders(List.of("*"));
      corsConfiguration.setAllowCredentials(true);
      UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
      source.registerCorsConfiguration("/**", corsConfiguration);
      return source;


  }
}
