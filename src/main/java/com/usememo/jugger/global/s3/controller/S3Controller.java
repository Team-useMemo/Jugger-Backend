package com.usememo.jugger.global.s3.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import com.usememo.jugger.domain.photo.dto.PhotoDto;
import com.usememo.jugger.global.s3.service.S3Service;
import com.usememo.jugger.global.s3.service.S3ServiceImplementation;
import com.usememo.jugger.global.security.CustomOAuth2User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/upload")
@Tag(name = "이미지 업로드 API", description = "이미지 업로드 API에 대한 설명입니다.")
@RequiredArgsConstructor
public class S3Controller {
	private final S3Service s3Service;

	@Operation(description = "[POST] 이미지 업로드")
	@PostMapping(value = "/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Mono<ResponseEntity<String>> upload(
		@RequestPart("file") FilePart file,
		@RequestPart("category_uuid") String categoryId,
		@AuthenticationPrincipal CustomOAuth2User customOAuth2User
	) {
		PhotoDto dto = PhotoDto.builder()
			.userId(customOAuth2User.getUserId())
			.categoryId(categoryId)
			.filePart(file)
			.build();

		return s3Service.uploadFile(dto)
			.map(url -> ResponseEntity.ok(url));
	}
}
