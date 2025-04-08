package com.usememo.jugger.domain.photo.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Document(collection = "photos")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Photo {
	@Id
	private String photo_uuid;
	private String user_uuid;
	private String url;
	private String category_uuid;
}
