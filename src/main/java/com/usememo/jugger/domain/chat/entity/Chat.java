package com.usememo.jugger.domain.chat.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Document(collection = "chats")
@AllArgsConstructor
@Builder
public class Chat {
	@Id
	private String uuid;
	private String userUuid;

	private String categoryName;
	private String data;
	private Refs refs;

	@Data
	public static class Refs {
		private String calendarUuid;
		private String photoUuid;
		private String linkUuid;
	}
}

