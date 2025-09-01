package com.usememo.jugger.domain.category.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public record ClassifyResponse(
	String category,
	@JsonProperty("recommend_category")
	List<String> recommendCategory,
	List<SentenceResult> sentences
) {}


