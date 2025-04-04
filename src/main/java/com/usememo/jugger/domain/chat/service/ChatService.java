package com.usememo.jugger.domain.chat.service;

import java.time.ZonedDateTime;
import java.util.List;

import com.usememo.jugger.domain.chat.dto.GetChatByCategoryDto;
import com.usememo.jugger.domain.chat.dto.PostChatDto;

import reactor.core.publisher.Mono;

public interface ChatService {

	// TODO: V2 AI 서버 연동시
	Mono<Void> processChat(PostChatDto postChatDto);

	Mono<Void> postChat(PostChatDto postChatDto);

	Mono<List<GetChatByCategoryDto>> getChatsBefore(ZonedDateTime before, int page, int size);

	Mono<List<GetChatByCategoryDto>> getChatsAfter(ZonedDateTime before, int page, int size);

}
