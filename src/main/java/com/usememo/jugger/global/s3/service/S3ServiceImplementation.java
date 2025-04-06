package com.usememo.jugger.global.s3.service;

import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;

import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3ServiceImplementation implements S3Service{

	private final S3Template s3Template;
	private static final String BUCKET_NAME = "jugger-bucket";

	public Mono<String> uploadFile(FilePart filePart) {
		String originalFilename = filePart.filename();
		String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
		String newFileName = UUID.randomUUID() + ext;

		return DataBufferUtils.join(filePart.content())
			.flatMap(dataBuffer -> {
				try (InputStream inputStream = dataBuffer.asInputStream()) {
					s3Template.upload(BUCKET_NAME, newFileName, inputStream);
					return Mono.just("https://" + BUCKET_NAME + ".s3.ap-northeast-2.amazonaws.com/" + newFileName);
				} catch (IOException e) {
					return Mono.error(new RuntimeException("파일 업로드 실패", e));
				}
			});
	}
}
