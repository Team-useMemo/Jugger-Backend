package com.usememo.jugger.domain.photo.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.usememo.jugger.global.utils.BaseTimeEntity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Document(collection = "photos")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class Photo extends BaseTimeEntity {
	@Id
	@Field("photo_uuid")
	private String photoUuid;

	@Field("user_uuid")
	private String userUuid;

	@Field("url")
	private String url;

	@Field("category_uuid")
	private String categoryUuid;

	@Field("description")
	private String description;

	public Photo() {
	}

}
