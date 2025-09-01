package com.usememo.jugger.global.config;


import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.unit.DataSize;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {

	@Bean
	public WebClient fastApiWebClient(@Value("${fastapi.base-url}") String baseUrl) {

		// 고정값
		int connectTimeoutMs = 10_000;   // 10초
		int responseTimeoutMs = 360_000; // 6분
		int maxInMemoryMb = 8;

		HttpClient httpClient = HttpClient.create()
			.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMs)
			.responseTimeout(Duration.ofMillis(responseTimeoutMs))
			.doOnConnected(conn -> {
				conn.addHandlerLast(new ReadTimeoutHandler(responseTimeoutMs, TimeUnit.MILLISECONDS));
				conn.addHandlerLast(new WriteTimeoutHandler(responseTimeoutMs, TimeUnit.MILLISECONDS));
			});

		ExchangeStrategies strategies = ExchangeStrategies.builder()
			.codecs(c -> c.defaultCodecs().maxInMemorySize(
				(int) DataSize.ofMegabytes(maxInMemoryMb).toBytes()))
			.build();

		return WebClient.builder()
			.baseUrl(baseUrl)
			.clientConnector(new ReactorClientHttpConnector(httpClient))
			.exchangeStrategies(strategies)
			.build();
	}
}