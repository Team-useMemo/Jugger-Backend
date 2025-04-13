package com.usememo.jugger.domain.photo.dto;

import java.time.Instant;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetPhotoDto {
	private String url;
	private String categoryName;
	private Instant timestamp;
}
