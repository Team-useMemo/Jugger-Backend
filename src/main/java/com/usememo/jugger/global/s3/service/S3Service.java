package com.usememo.jugger.global.s3.service;

import org.springframework.http.codec.multipart.FilePart;

import com.usememo.jugger.domain.photo.dto.PhotoDto;
import com.usememo.jugger.global.s3.dto.FilesUploadResponse;
import com.usememo.jugger.global.security.CustomOAuth2User;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface S3Service {
	Mono<String> uploadFile(PhotoDto photoDto);

	Mono<FilesUploadResponse> uploadFiles(Flux<FilePart> files, CustomOAuth2User customOAuth2User, String categoryId);
}
