package com.usememo.jugger.global.s3.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import com.usememo.jugger.domain.photo.dto.PhotoDto;
import com.usememo.jugger.global.s3.service.S3Service;
import com.usememo.jugger.global.s3.service.S3ServiceImplementation;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class S3Controller {
	private final S3Service s3Service;

	@PostMapping(value = "/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Mono<ResponseEntity<String>> upload(
		@RequestPart("file") FilePart file,
		@RequestPart("category_uuid") String categoryUuid
	) {
		PhotoDto dto = PhotoDto.builder()
			.user_uuid("123456789a")
			.category_uuid(categoryUuid)
			.filePart(file)
			.build();

		return s3Service.uploadFile(dto)
			.map(url -> ResponseEntity.ok(url));
	}
}
