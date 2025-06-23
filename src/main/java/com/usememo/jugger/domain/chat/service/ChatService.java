package com.usememo.jugger.domain.chat.service;

import java.time.Instant;
import java.util.List;

import com.usememo.jugger.domain.chat.dto.GetChatByCategoryDto;
import com.usememo.jugger.domain.chat.dto.PostChatDto;
import com.usememo.jugger.domain.chat.dto.PostChatTextDto;
import com.usememo.jugger.domain.chat.entity.Chat;
import com.usememo.jugger.global.security.CustomOAuth2User;

import reactor.core.publisher.Mono;

public interface ChatService {

	// TODO: V2 AI 서버 연동시
	Mono<Void> processChat(PostChatDto postChatDto, CustomOAuth2User customOAuth2User);

	Mono<Void> postChat(PostChatDto postChatDto, CustomOAuth2User customOAuth2User);

	Mono<String> postChatWithoutCategory(PostChatTextDto postChatTextDto, CustomOAuth2User customOAuth2User);

	Mono<List<GetChatByCategoryDto>> getChatsBefore(Instant before, int page, int size,
		CustomOAuth2User customOAuth2User);

	Mono<List<GetChatByCategoryDto>> getChatsAfter(Instant after, int page, int size,
		CustomOAuth2User customOAuth2User);

	Mono<List<GetChatByCategoryDto>> getChatsByCategoryIdBefore(String categoryId, Instant before, int page, int size);

	Mono<List<GetChatByCategoryDto>> getChatsByCategoryIdAfter(String categoryId, Instant after, int page, int size);

	Mono<Chat> getLatestChatByCategoryId(String categoryId);

	Mono<Void> deleteAllChats(CustomOAuth2User customOAuth2User);
}
