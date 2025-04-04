package com.usememo.jugger.domain.chat.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.usememo.jugger.global.utils.BaseTimeEntity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Document(collection = "chats")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class Chat extends BaseTimeEntity {
	@Id
	private String uuid;
	private String userUuid;

	private String categoryUuid;
	private String data;
	private Refs refs;

	@Data
	@Builder
	public static class Refs {
		private String calendarUuid;
		private String photoUuid;
		private String linkUuid;
	}
}

