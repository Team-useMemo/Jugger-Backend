package com.usememo.jugger.domain.category.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public record SentenceResult(
	String text,
	List<String> urls,
	@JsonProperty("invalid_urls")
	List<String> invalidUrls,
	// schedules 구조가 리스트/객체/널 등 유동적일 수 있어 JsonNode로 받는 것을 권장
	JsonNode schedules
) {}