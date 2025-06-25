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
import com.usememo.jugger.global.s3.dto.FileUploadResponse;
import com.usememo.jugger.global.s3.dto.FilesUploadResponse;
import com.usememo.jugger.global.s3.service.S3Service;
import com.usememo.jugger.global.s3.service.S3ServiceImplementation;
import com.usememo.jugger.global.security.CustomOAuth2User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/upload")
@Tag(name = "이미지 업로드 API", description = "이미지 업로드 API에 대한 설명입니다.")
@RequiredArgsConstructor
public class S3Controller {
	private final S3Service s3Service;

	@Operation(summary = "[POST] 이미지 업로드")
	@PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Mono<ResponseEntity<FileUploadResponse>> upload(
		@RequestPart("file") FilePart file,
		@RequestPart("categoryId") String categoryId,
		@RequestPart("description") String description,
		@AuthenticationPrincipal CustomOAuth2User customOAuth2User
	) {
		PhotoDto dto = PhotoDto.builder()
			.userId(customOAuth2User.getUserId())
			.categoryId(categoryId)
			.filePart(file)
			.description(description)
			.build();

		return s3Service.uploadFile(dto)
			.map(url -> ResponseEntity.ok().body(new FileUploadResponse(200,"이미지가 업로드되었습니다.",url)));
	}


	@Operation(summary = "[POST] 이미지 여러 장 업로드")
	@PostMapping(value = "/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Mono<ResponseEntity<FilesUploadResponse>> uploadPhotos( @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
		@RequestPart("files") Flux<FilePart> files,
		@RequestPart("categoryId") String categoryId,
		@RequestPart("description") String description
	){
		return s3Service.uploadFiles(files,customOAuth2User,categoryId)
			.map(ans ->  ResponseEntity.ok().body(ans));
	}

}
