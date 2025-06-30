package com.usememo.jugger.domain.chat.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.mongodb.core.mapping.Document;

import com.usememo.jugger.global.utils.BaseTimeEntity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Document(collection = "chats")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class Chat extends BaseTimeEntity implements Persistable<String> {


	@Id
	private String uuid;
	private String userUuid;

	@Setter
	private String categoryUuid;
	@Setter
	private String data;
	private Refs refs;

	@Data
	@Builder
	public static class Refs {
		private String calendarUuid;
		private String photoUuid;
		private String linkUuid;
	}

	@Override
	public String getId() {
		return getUuid();
	}

	@Override
	public boolean isNew() {
		return getCreatedAt() == null;
	}
}

