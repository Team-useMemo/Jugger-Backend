package com.usememo.jugger.domain.chat.controller;

import java.time.Instant;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.usememo.jugger.domain.chat.dto.GetChatByCategoryDto;
import com.usememo.jugger.domain.chat.dto.PostChatDto;
import com.usememo.jugger.domain.chat.service.ChatService;
import com.usememo.jugger.global.security.CustomOAuth2User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@Tag(name = "채팅 API", description = "채팅 API에 대한 설명입니다.")
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {
	private final ChatService chatService;

	@Operation(summary = "[POST]채팅 입력 ")
	@PostMapping
	public Mono<ResponseEntity<Void>> postChat(@RequestBody PostChatDto postChatDto, @AuthenticationPrincipal
	CustomOAuth2User customOAuth2User) {
		return chatService.postChat(postChatDto, customOAuth2User)
			.thenReturn(ResponseEntity.ok().build());
	}

	@Operation(summary = "[GET] 전체 채팅 조회(이전 메세지 조회)")
	@GetMapping("/before")
	public Mono<ResponseEntity<List<GetChatByCategoryDto>>> getChatsBefore(
		@RequestParam("before") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant before,
		@RequestParam(value = "page", defaultValue = "0") int page,
		@RequestParam(value = "size", defaultValue = "20") int size, @AuthenticationPrincipal
	CustomOAuth2User customOAuth2User) {
		log.info(customOAuth2User.getUserId());
		return chatService.getChatsBefore(before, page, size, customOAuth2User)
			.map(list -> ResponseEntity.ok().body(list));

	}

	@Operation(summary = "[GET] 전체 채팅 조회(이후 메세지 조회)")
	@GetMapping("/after")
	public Mono<ResponseEntity<List<GetChatByCategoryDto>>> getChatsAfter(
		@RequestParam("after") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant after,
		@RequestParam(value = "page", defaultValue = "0") int page,
		@RequestParam(value = "size", defaultValue = "20") int size, @AuthenticationPrincipal
	CustomOAuth2User customOAuth2User) {
		return chatService.getChatsAfter(after, page, size, customOAuth2User)
			.map(list -> ResponseEntity.ok().body(list));

	}
}
