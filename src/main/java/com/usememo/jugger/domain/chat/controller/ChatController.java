package com.usememo.jugger.domain.chat.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
		return chatService.processChat(postChatDto)
			.thenReturn(ResponseEntity.ok().build());
	}
}
