package com.usememo.jugger.global.s3.service;

import org.springframework.http.codec.multipart.FilePart;

import com.usememo.jugger.domain.photo.dto.PhotoDto;

import reactor.core.publisher.Mono;

public interface S3Service {
	Mono<String> uploadFile(PhotoDto photoDto);
}
