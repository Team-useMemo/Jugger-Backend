package com.usememo.jugger.domain.category.controller;

import java.time.Instant;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.usememo.jugger.domain.category.dto.PostCategoryDto;
import com.usememo.jugger.domain.category.entity.Category;
import com.usememo.jugger.domain.category.service.CategoryService;
import com.usememo.jugger.domain.chat.dto.GetChatByCategoryDto;
import com.usememo.jugger.domain.chat.service.ChatService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

	private final CategoryService categoryService;

	private final ChatService chatService;

	@PostMapping
	public Mono<ResponseEntity<Category>> createCategory(@RequestBody PostCategoryDto postCategoryDto) {
		return categoryService.createCategory(postCategoryDto)
			.map(savedCategory -> ResponseEntity
				.status(HttpStatus.CREATED)
				.body(savedCategory));
	}

	@GetMapping("/chat/before")
	public Mono<ResponseEntity<List<GetChatByCategoryDto>>> getChatsByCategoryBefore(
		@RequestParam("categoryId") String categoryId,
		@RequestParam("before") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant before,
		@RequestParam(value = "page", defaultValue = "0") int page,
		@RequestParam(value = "size", defaultValue = "20") int size) {
		return chatService.getChatsByCategoryIdBefore(categoryId, before, page, size)
			.map(list -> ResponseEntity.ok().body(list));

	}

	@GetMapping("/chat/after")
	public Mono<ResponseEntity<List<GetChatByCategoryDto>>> getChatsByCategoryAfter(
		@RequestParam("categoryId") String categoryId,
		@RequestParam("after") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant after,
		@RequestParam(value = "page", defaultValue = "0") int page,
		@RequestParam(value = "size", defaultValue = "20") int size) {
		return chatService.getChatsByCategoryIdAfter(categoryId, after, page, size)
			.map(list -> ResponseEntity.ok().body(list));

	}
}