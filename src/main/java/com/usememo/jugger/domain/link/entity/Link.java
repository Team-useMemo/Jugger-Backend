package com.usememo.jugger.domain.link.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Document(collection = "links")
@AllArgsConstructor
@Builder
public class Link {
	@Id
	private String uuid;
	private String userUuid;
	private String chatUuid;

	private String url;
	private Category category;

	@Data
	public static class Category {
		private String categoryUuid;
		private String name;
	}
}

