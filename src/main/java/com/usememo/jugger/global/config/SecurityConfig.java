package com.usememo.jugger.global.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.web.server.util.matcher.AndServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import com.usememo.jugger.global.security.JwtAuthenticationConverter;
import com.usememo.jugger.global.security.JwtAuthenticationManager;
import com.usememo.jugger.global.security.OAuth2AuthenticationSuccessHandler;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

	@Bean
	public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
		OAuth2AuthenticationSuccessHandler successHandler,
		JwtAuthenticationManager jwtAuthenticationManager,
		JwtAuthenticationConverter jwtAuthenticationConverter) {

		AuthenticationWebFilter jwtFilter = new AuthenticationWebFilter(jwtAuthenticationManager);
		jwtFilter.setSecurityContextRepository(NoOpServerSecurityContextRepository.getInstance());
		jwtFilter.setServerAuthenticationConverter(jwtAuthenticationConverter);
		jwtFilter.setRequiresAuthenticationMatcher(
			new AndServerWebExchangeMatcher(
				ServerWebExchangeMatchers.pathMatchers("/api/**"),
				new NegatedServerWebExchangeMatcher(
					ServerWebExchangeMatchers.pathMatchers("/api/v3/api-docs/**")
				)
			)
		);

		return http
			.cors(ServerHttpSecurity.CorsSpec::disable)
			.csrf(ServerHttpSecurity.CsrfSpec::disable)
			.authorizeExchange(exchange -> exchange
				.pathMatchers("/", "/login/**", "/oauth2/**", "/auth/**", "/swagger-ui.html", "/swagger-ui/**",
					"/v3/api-docs/**", "/webjars/**", "/favicon.ico", "/docs", "/health/**", "/api/v3/api-docs/**")
				.permitAll()
				.anyExchange().authenticated()
			)
			.addFilterAt(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)
			.build();
	}

}
