package com.usememo.jugger.domain.link.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Document(collection = "links")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Link {
	@Id
	private String uuid;
	private String userUuid;
	private String url;
	private String categoryUuid;
}

