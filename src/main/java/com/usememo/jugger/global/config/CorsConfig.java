package com.usememo.jugger.global.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.WebFilter;

@Configuration
public class CorsConfig {

	private static final List<String> ALLOWED_ORIGINS = List.of(
		"https://jugger.netlify.app",
		"http://localhost:5173",
		"http://localhost:3000"
	);

	@Bean
	public WebFilter corsFilter() {
		return (exchange, chain) -> {
			ServerHttpRequest request = exchange.getRequest();
			ServerHttpResponse response = exchange.getResponse();
			String origin = request.getHeaders().getOrigin();

			if (origin != null && ALLOWED_ORIGINS.contains(origin)) {
				response.getHeaders().add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
				response.getHeaders().add(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, DELETE, OPTIONS");
				response.getHeaders().add(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "Origin, Content-Type, Accept, Authorization");
				response.getHeaders().add(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
			}

			if (request.getMethod() == HttpMethod.OPTIONS) {
				response.setStatusCode(HttpStatus.OK);
				return response.setComplete();
			}

			return chain.filter(exchange);
		};
	}

}
