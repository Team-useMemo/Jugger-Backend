package com.usememo.jugger.domain.link.controller;

import java.time.Instant;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.usememo.jugger.domain.link.dto.GetLinkDto;
import com.usememo.jugger.domain.link.dto.LinkListResponse;
import com.usememo.jugger.domain.link.dto.LinkRequest;
import com.usememo.jugger.domain.link.dto.LinkResponse;
import com.usememo.jugger.domain.link.dto.LinkUpdateRequest;
import com.usememo.jugger.domain.link.service.LinkService;
import com.usememo.jugger.global.response.BaseResponse;
import com.usememo.jugger.global.security.CustomOAuth2User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@Tag(name = "링크 API", description = "링크 API에 대한 설명입니다.")
@RequestMapping("/api/v1/links")
@RequiredArgsConstructor
public class LinkController {

	private final LinkService linkService;

	@Operation(summary = "[GET] 카테고리 포함 링크 조회")
	@GetMapping("/category")
	public Mono<ResponseEntity<List<GetLinkDto.LinkData>>> getLinks(@RequestParam("categoryId") String categoryUuid,
		@RequestParam("before") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant before,
		@RequestParam(value = "page", defaultValue = "0") int page,
		@RequestParam(value = "size", defaultValue = "20") int size,
		@AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
		return linkService.getLinks(before,page,size,customOAuth2User,categoryUuid)
			.map(list -> ResponseEntity.ok().body(list));
	}

	@Operation(summary = "[GET] 카테고리 없이 링크 조회")
	@GetMapping("")
	public Mono<ResponseEntity<List<LinkListResponse>>> getLinksNoCategory(@RequestParam("before") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant before,
		@RequestParam(value = "page", defaultValue = "0") int page,
		@RequestParam(value = "size", defaultValue = "20") int size,
		@AuthenticationPrincipal CustomOAuth2User customOAuth2User){
		return linkService.getLinksNoCategory(before, page, size, customOAuth2User)
			.map(responses->ResponseEntity.ok().body(responses));
	}


	@Operation(summary = "[POST] 링크 등록")
	@PostMapping()
	public Mono<ResponseEntity<LinkResponse>> postLink(@AuthenticationPrincipal CustomOAuth2User customOAuth2User,
		@RequestBody LinkRequest request
		){
		return linkService.postLink(customOAuth2User,request)
			.map( response-> ResponseEntity.ok().body(response));

	}

	@Operation(summary = "[PATCH] 링크 변경")
	@PatchMapping()
	public Mono<ResponseEntity<LinkResponse>> updateLink(@AuthenticationPrincipal CustomOAuth2User customOAuth2User,
		@RequestBody LinkUpdateRequest linkUpdateRequest){
		return linkService.updateLink(customOAuth2User,linkUpdateRequest)
			.map(response-> ResponseEntity.ok().body(response));
	}

	@Operation(summary = "[DELETE] 링크 id로 삭제")
	@DeleteMapping("")
	public Mono<ResponseEntity<BaseResponse>> deleteLink(
		@AuthenticationPrincipal CustomOAuth2User customOAuth2User,
		@RequestParam String linkId){
		return linkService.deleteByLinkId(customOAuth2User,linkId)
			.map(baseResponse -> ResponseEntity.ok().body(baseResponse));
	}

}
