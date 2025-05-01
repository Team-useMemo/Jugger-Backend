package com.usememo.jugger.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import com.usememo.jugger.global.security.CustomOAuth2UserService;
import com.usememo.jugger.global.security.OAuth2AuthenticationSuccessHandler;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

	private final CustomOAuth2UserService customOAuth2UserService;

	public SecurityConfig(CustomOAuth2UserService customOAuth2UserService) {
		this.customOAuth2UserService = customOAuth2UserService;
	}

	@Bean
	public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
		OAuth2AuthenticationSuccessHandler successHandler) {
		return http
			.csrf(ServerHttpSecurity.CsrfSpec::disable)
			.authorizeExchange(exchange -> exchange
				.pathMatchers(
					"/",
					"/login/**",
					"/oauth2/**",
					"/auth/**",
					"/swagger-ui.html",
					"/swagger-ui/**",
					"/v3/api-docs/**",
					"/webjars/**",
					"/favicon.ico"
				)
				.permitAll()
				.anyExchange()
				.authenticated()
			)

			.build();
	}
}
