package com.usememo.jugger.domain.chat.controller;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.usememo.jugger.domain.chat.dto.GetChatByCategoryDto;
import com.usememo.jugger.domain.chat.dto.PostChatDto;
import com.usememo.jugger.domain.chat.service.ChatService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {
	private final ChatService chatService;

	@PostMapping
	public Mono<ResponseEntity<Void>> postChat(@RequestBody PostChatDto postChatDto) {
		return chatService.postChat(postChatDto)
			.thenReturn(ResponseEntity.ok().build());
	}

	@GetMapping("/before")
	public Mono<ResponseEntity<List<GetChatByCategoryDto>>> getChatsBefore(
		@RequestParam("before") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime before,
		@RequestParam(value = "page", defaultValue = "0") int page,
		@RequestParam(value = "size", defaultValue = "20") int size) {
		return chatService.getChatsBefore(before, page, size)
			.map(list -> ResponseEntity.ok().body(list));

	}

	@GetMapping("after")
	public Mono<ResponseEntity<List<GetChatByCategoryDto>>> getChatsAfter(
		@RequestParam("after") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime after,
		@RequestParam(value = "page", defaultValue = "0") int page,
		@RequestParam(value = "size", defaultValue = "20") int size) {
		return chatService.getChatsAfter(after, page, size)
			.map(list -> ResponseEntity.ok().body(list));

	}
}
