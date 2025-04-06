package com.usememo.jugger.global.s3.service;

import org.springframework.http.codec.multipart.FilePart;

import reactor.core.publisher.Mono;

public interface S3Service {
	Mono<String> uploadFile(FilePart filePart);
}
