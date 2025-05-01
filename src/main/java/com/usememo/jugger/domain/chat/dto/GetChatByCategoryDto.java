package com.usememo.jugger.domain.chat.dto;

import java.time.Instant;
import java.util.List;

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

		@Schema(description = "채팅 텍스트")
		private String data;
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
		@Schema(description = "채팅 시간")
		private Instant timestamp;
	}

}
