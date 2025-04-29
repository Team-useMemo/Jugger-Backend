package com.usememo.jugger.domain.category.dto;

import java.time.Instant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetRecentCategoryDto {

	private String uuid;
	private String name;
	private String color;
	private Boolean isPinned;
	private Instant updateAt;
	private String recentMessage;
}
