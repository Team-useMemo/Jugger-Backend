package com.usememo.jugger.domain.category.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostCategoryDto {
	private String name;
	private String color;
}
