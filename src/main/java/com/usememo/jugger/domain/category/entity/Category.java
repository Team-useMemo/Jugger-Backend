package com.usememo.jugger.domain.category.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.usememo.jugger.global.utils.BaseTimeEntity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Document(collection = "category")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
public class Category extends BaseTimeEntity {
	@Id
	private String uuid;
	private String name;
	private String color;
	private Boolean isPinned;
	private String userUuid;
}
