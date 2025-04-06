package com.usememo.jugger.global.s3.service;


import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.util.UUID;

import com.usememo.jugger.global.exception.s3.S3UploadException;

@Service
@RequiredArgsConstructor
public class S3ServiceImplementation implements S3Service{

	private final S3Template s3Template;
	@Value("${spring.cloud.aws.s3.bucket}")
	private String bucketName;

	public Mono<String> uploadFile(FilePart filePart) {
		String originalFilename = filePart.filename();
		String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
		String newFileName = UUID.randomUUID() + ext;

		return DataBufferUtils.join(filePart.content())
			.flatMap(dataBuffer -> {
				try (InputStream inputStream = dataBuffer.asInputStream()) {
					s3Template.upload(bucketName, newFileName, inputStream);
					return Mono.just("https://" + bucketName + ".s3.ap-northeast-2.amazonaws.com/" + newFileName);
				} catch (Exception e) {
					return Mono.error(new S3UploadException());
				}
			});
	}
}
