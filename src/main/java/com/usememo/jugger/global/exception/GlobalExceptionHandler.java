package com.usememo.jugger.global.exception;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.usememo.jugger.global.exception.BaseException;
import com.usememo.jugger.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
@Order(-2)
@RequiredArgsConstructor
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

	private final ObjectMapper objectMapper;

	@Override
	public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
		ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
		if (ex instanceof BaseException) {
			errorCode = ((BaseException) ex).getErrorCode();
		}

		exchange.getResponse().setStatusCode(errorCode.getHttpStatus());
		exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

		Map<String, Object> response = new HashMap<>();
		response.put("code", errorCode.getCode());
		response.put("message", errorCode.getMessage());

		DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();

		try {
			byte[] bytes = objectMapper.writeValueAsBytes(response);
			return exchange.getResponse().writeWith(Mono.just(bufferFactory.wrap(bytes)));
		} catch (Exception e) {
			byte[] fallback = "예외 처리 중 오류가 발생했습니다.".getBytes(StandardCharsets.UTF_8);
			return exchange.getResponse().writeWith(Mono.just(bufferFactory.wrap(fallback)));
		}
	}
}
