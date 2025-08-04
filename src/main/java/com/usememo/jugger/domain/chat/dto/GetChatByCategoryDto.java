package com.usememo.jugger.domain.chat.dto;

import java.time.Instant;
import java.util.List;

import ch.qos.logback.core.status.InfoStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "채팅 조회 DTO")
public class GetChatByCategoryDto {
	@Schema(description = "카테고리 ID")
	private String categoryId;
	@Schema(description = "카테고리 이름")
	private String categoryName;

	@Schema(description = "카테고리 색상")
	private String categoryColor;

	private List<ChatItem> chatItems;

	@Data
	@Builder
	public static class ChatItem {
		@Schema(description = "채팅 id")
		private String chatId;
		@Schema(description = "카테고리 id")
		private String categoryId;
		@Schema(description = "채팅 타입")
		private String type;
		@Schema(description = "채팅 텍스트")
		private String content;
		@Schema(description = "이미지 URL")
		private String imgUrl;
		@Schema(description = "링크 URL")
		private String linkUrl;
		@Schema(description = "일정 제목")
		private String scheduleName;
		@Schema(description = "일정 시작 날짜")
		private Instant scheduleStartDate;
		@Schema(description = "일정 종료 날짜")
		private Instant scheduleEndDate;
		@Schema(description = "일정 장소")
		private String place;
		@Schema(description = "일정 알람 시간")
		private Instant alarm;
		@Schema(description = "일정 내용")
		private String description;
		@Schema(description = "채팅 시간")
		private Instant timestamp;
	}

}
