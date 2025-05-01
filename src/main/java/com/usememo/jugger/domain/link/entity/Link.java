package com.usememo.jugger.domain.link.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Document(collection = "links")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class Link {
	@Id
	private String uuid;
	private String userUuid;
	private String chatUuid;

	private String url;
	private String categoryUuid;

	private String caption;

	public Link() {
	}
}

