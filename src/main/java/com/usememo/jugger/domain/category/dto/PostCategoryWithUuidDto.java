package com.usememo.jugger.domain.category.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostCategoryWithUuidDto {
	private String categoryUuid;
	private String name;
	private String color;
}
