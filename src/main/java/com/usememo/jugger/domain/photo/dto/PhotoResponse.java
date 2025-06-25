package com.usememo.jugger.domain.photo.dto;

import java.time.Instant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PhotoResponse {
	private String url;
	private String categoryId;
	private String description;
	private Instant timestamp;
}
