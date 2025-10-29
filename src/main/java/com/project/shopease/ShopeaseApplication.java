package com.project.shopease;

import java.util.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@SpringBootApplication
public class ShopeaseApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShopeaseApplication.class, args);
	}

	@Bean
	public CorsFilter corsFilter() {
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		CorsConfiguration config = new CorsConfiguration();

		config.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
		config.setAllowCredentials(true);

		config.setAllowedHeaders(Arrays.asList(
				"Origin", "Content-Type", "Accept", "Authorization",
				"X-Requested-With", "content-range", "range"));

		config.setExposedHeaders(Arrays.asList(
				"Content-Range", "X-Total-Count", "Content-Type", "Accept"));

		config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

		source.registerCorsConfiguration("/**", config);
		return new CorsFilter(source);
	}

}