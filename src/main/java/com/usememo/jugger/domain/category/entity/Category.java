package com.usememo.jugger.domain.category.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Document(collection = "category")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Category {
	@Id
	private String uuid;
	private String name;
	private String color;
}
