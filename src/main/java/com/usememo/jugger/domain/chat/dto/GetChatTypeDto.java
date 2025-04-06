package com.usememo.jugger.domain.chat.dto;

import com.usememo.jugger.domain.chat.entity.ChatType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetChatTypeDto {

	private ChatType chatType;
}
