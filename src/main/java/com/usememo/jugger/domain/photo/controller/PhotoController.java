package com.usememo.jugger.domain.photo.controller;

import java.time.Instant;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.usememo.jugger.domain.photo.dto.PhotoResponse;
import com.usememo.jugger.domain.photo.dto.GetPhotoRequestDto;
import com.usememo.jugger.domain.photo.dto.PhotoUpdateRequest;
import com.usememo.jugger.domain.photo.service.PhotoService;
import com.usememo.jugger.global.response.BaseResponse;
import com.usememo.jugger.global.security.CustomOAuth2User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/photos")
@Tag(name = "사진 API", description = "사진 API에 대한 설명입니다.")
@RequiredArgsConstructor
public class PhotoController {

	private final PhotoService photoService;

	@Operation(summary = "[GET] 사진 조회")
	@GetMapping()
	public Flux<PhotoResponse> getPhotos(
		@RequestParam String categoryId, @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
		return photoService.getPhotoDto(GetPhotoRequestDto.builder()
			.categoryId(categoryId)
			.build(), customOAuth2User);
	}

	@Operation(summary = "[GET] 이전 기간별 사진 조회")
	@GetMapping("/duration")
	public ResponseEntity<Flux<PhotoResponse>> getPhotoDuration(@RequestParam("before") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant before,
		@RequestParam(value = "page", defaultValue = "0") int page,
		@RequestParam(value = "size", defaultValue = "20") int size,@AuthenticationPrincipal CustomOAuth2User customOAuth2User){
		return ResponseEntity.ok().body(photoService.getPhotoDuration(before,page,size,customOAuth2User));
	}

	@Operation(summary = "[GET] 카테고리와 기간별 사진 조회")
	@GetMapping("/category")
	public ResponseEntity<Flux<PhotoResponse>> getPhotoCategoryAndDuration(
		@RequestParam("categoryId") String categoryId,
		@RequestParam("before")@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant before,
		@RequestParam(value = "page", defaultValue = "0") int page,
		@RequestParam(value = "size", defaultValue = "20") int size,
		@AuthenticationPrincipal CustomOAuth2User customOAuth2User
		){
		return ResponseEntity.ok().body(photoService.getPhotoCategoryAndDuration(categoryId,before, page, size, customOAuth2User));
	}

	@Operation(summary = "[PATCH] 사진 description 수정")
	@PatchMapping()
	public Mono<ResponseEntity<BaseResponse>> updatePhoto(
		@AuthenticationPrincipal CustomOAuth2User customOAuth2User,
		@RequestBody PhotoUpdateRequest request
	){
		return photoService.updatePhotoDescription(customOAuth2User,request)
			.map(response -> ResponseEntity.ok().body(response));
	}

	@Operation(summary = "[DELETE] 사진 id로 삭제")
	@DeleteMapping("")
	public Mono<ResponseEntity<BaseResponse>> deletePhoto(
		@AuthenticationPrincipal CustomOAuth2User customOAuth2User,
		@RequestParam String photoId){

		return photoService.deleteByPhotoId(customOAuth2User,photoId)
			.map(baseResponse -> ResponseEntity.ok().body(baseResponse));
	}


}
