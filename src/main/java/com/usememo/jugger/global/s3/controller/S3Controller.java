package com.usememo.jugger.global.s3.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import com.usememo.jugger.global.s3.service.S3ServiceImplementation;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class S3Controller {
	private final S3ServiceImplementation s3ServiceImplementation;

	@PostMapping(value = "/test", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Mono<ResponseEntity<String>> upload(@RequestPart("file") FilePart file) {
		return s3ServiceImplementation.uploadFile(file)
			.map(url -> ResponseEntity.ok(url));
	}
}
