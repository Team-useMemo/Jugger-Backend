package com.usememo.jugger.domain.chat.dto;

import java.time.Instant;
import java.util.List;

import com.usememo.jugger.domain.chat.entity.Chat;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetChatByCategoryDto {
	private String categoryId;
	private String categoryName;
	private String categoryColor;

	private List<ChatItem> chatItems;

	@Data
	@Builder
	public static class ChatItem {
		private String data;
		private Chat.Refs calendar;
		private Chat.Refs photo;
		private Chat.Refs link;
		private Instant timestamp;
	}

}
