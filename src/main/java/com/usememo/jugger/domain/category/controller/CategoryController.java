package com.usememo.jugger.domain.category.controller;

import java.time.Instant;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.client.result.UpdateResult;
import com.usememo.jugger.domain.category.dto.DeleteResponse;
import com.usememo.jugger.domain.category.dto.GetRecentCategoryDto;
import com.usememo.jugger.domain.category.dto.PostCategoryDto;
import com.usememo.jugger.domain.category.dto.PostCategoryWithUuidDto;
import com.usememo.jugger.domain.category.dto.UpdateRequest;
import com.usememo.jugger.domain.category.dto.UpdateResponse;
import com.usememo.jugger.domain.category.entity.Category;
import com.usememo.jugger.domain.category.service.CategoryService;
import com.usememo.jugger.domain.chat.dto.GetChatByCategoryDto;
import com.usememo.jugger.domain.chat.service.ChatService;
import com.usememo.jugger.global.security.CustomOAuth2User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/categories")
@Tag(name = "카테고리 API", description = "카테고리 API에 대한 설명입니다.")
@RequiredArgsConstructor
public class CategoryController {

	private final CategoryService categoryService;

	private final ChatService chatService;

	@Operation(summary = "[POST] 카테고리 생성")
	@PostMapping
	public Mono<ResponseEntity<Category>> createCategory(@RequestBody PostCategoryDto postCategoryDto,
		@AuthenticationPrincipal
		CustomOAuth2User customOAuth2User) {
		return categoryService.createCategory(postCategoryDto, customOAuth2User)
			.map(savedCategory -> ResponseEntity
				.status(HttpStatus.CREATED)
				.body(savedCategory));
	}

	@Operation(summary = "[POST] 채팅방이 있는(카테고리 uuid가 있는) 상황에서 카테고리 생성")
	@PostMapping("/withChatRoom")
	public Mono<ResponseEntity<Category>> createCategoryWithUuid(
		@RequestBody PostCategoryWithUuidDto postCategoryWithUuidDto,
		@AuthenticationPrincipal
		CustomOAuth2User customOAuth2User) {
		return categoryService.createCategoryWithUuid(postCategoryWithUuidDto, customOAuth2User)
			.map(savedCategory -> ResponseEntity
				.status(HttpStatus.CREATED)
				.body(savedCategory));
	}

	@Operation(summary = "[GET] 카테고리별 채팅 조회 (이전 메세지 불러오기) ")
	@GetMapping("/chat/before")
	public Mono<ResponseEntity<List<GetChatByCategoryDto>>> getChatsByCategoryBefore(
		@AuthenticationPrincipal CustomOAuth2User customOAuth2User,
		@RequestParam("categoryId") String categoryId,
		@RequestParam("before") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant before,
		@RequestParam(value = "page", defaultValue = "0") int page,
		@RequestParam(value = "size", defaultValue = "20") int size) {
		return chatService.getChatsByCategoryIdBefore(customOAuth2User,categoryId, before, page, size)
			.map(list -> ResponseEntity.ok().body(list));

	}

	@Operation(summary = "[GET] 카테고리별 채팅 조회 (이후 메세지 불러오기)")
	@GetMapping("/chat/after")
	public Mono<ResponseEntity<List<GetChatByCategoryDto>>> getChatsByCategoryAfter(
		@AuthenticationPrincipal CustomOAuth2User customOAuth2User,
		@RequestParam("categoryId") String categoryId,
		@RequestParam("after") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant after,
		@RequestParam(value = "page", defaultValue = "0") int page,
		@RequestParam(value = "size", defaultValue = "20") int size) {
		return chatService.getChatsByCategoryIdAfter(customOAuth2User,categoryId, after, page, size)
			.map(list -> ResponseEntity.ok().body(list));

	}

	@Operation(summary = "[GET] 최근 채팅한 카테고리 조회 (사이드바 용)")
	@GetMapping("/recent")
	public Mono<ResponseEntity<List<GetRecentCategoryDto>>> getRecentCategories(
		@AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
		return categoryService.getRecentCategories(customOAuth2User)
			.collectList()
			.map(ResponseEntity::ok);
	}

	@Operation(summary = "[POST] 카테고리 핀 설정/해제")
	@PostMapping("/pin")
	public Mono<ResponseEntity<UpdateResult>> pinCategory(String categoryId, boolean isPinned) {
		return categoryService.pinCategory(categoryId, isPinned)
			.map(savedPin -> ResponseEntity
				.status(HttpStatus.OK)
				.body(savedPin));

	}

	@Operation(summary = "[DELETE] 카테고리 삭제")
	@DeleteMapping("/delete/{categoryId}")
	public Mono<ResponseEntity<DeleteResponse>> deleteCategory(@PathVariable String categoryId
		, @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
		return categoryService.deleteCategory(categoryId, customOAuth2User)
			.map(c -> ResponseEntity.status(HttpStatus.OK).body(new DeleteResponse(200, "카테고리가 삭제되었습니다.")));
	}

	@Operation(summary = "[PUT] 카테고리 수정")
	@PutMapping("update")
	public Mono<ResponseEntity<UpdateResponse>> updateCategory(@RequestBody UpdateRequest updateRequest,
		@AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
		return categoryService.updateCategory(updateRequest, customOAuth2User)
			.map(c -> ResponseEntity.status(HttpStatus.OK).body(c));
	}

}