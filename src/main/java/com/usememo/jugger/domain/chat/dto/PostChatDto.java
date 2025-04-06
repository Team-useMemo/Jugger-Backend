package com.usememo.jugger.domain.chat.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PostChatDto {
	private String categoryUuid;
	private String text;
}
