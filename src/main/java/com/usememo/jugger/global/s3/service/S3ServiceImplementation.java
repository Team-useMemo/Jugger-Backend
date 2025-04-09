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

import com.usememo.jugger.domain.photo.dto.PhotoDto;
import com.usememo.jugger.domain.photo.entity.Photo;
import com.usememo.jugger.domain.photo.repository.PhotoRepository;
import com.usememo.jugger.global.exception.s3.S3UploadException;

@Service
@RequiredArgsConstructor
public class S3ServiceImplementation implements S3Service{

	private final S3Template s3Template;
	private final PhotoRepository photoRepository;
	@Value("${spring.cloud.aws.s3.bucket}")
	private String bucketName;


	public Mono<String> uploadFile(PhotoDto photoDto) {
		String originalFilename = photoDto.getFilePart().filename();
		String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
		String newFileName = UUID.randomUUID() + ext;

		return DataBufferUtils.join(photoDto.getFilePart().content())
			.flatMap(dataBuffer -> {
				try (InputStream inputStream = dataBuffer.asInputStream()) {
					s3Template.upload(bucketName, newFileName, inputStream);
					String saveUrl = "https://" + bucketName + ".s3.ap-northeast-2.amazonaws.com/" + newFileName;

					return savePhoto(saveUrl, photoDto.getUser_uuid(), photoDto.getCategory_uuid())
						.flatMap(success -> {
							if (success) {
								return Mono.just(saveUrl);
							} else {
								return Mono.error(new S3UploadException());
							}
						});
				} catch (Exception e) {
					return Mono.error(new S3UploadException());
				}
			});
	}

	private Mono<Boolean> savePhoto(String saveUrl,String user_uuid, String category_uuid){
		return photoRepository.save(Photo.builder()
				.url(saveUrl)
				.userUuid(user_uuid)
				.categoryUuid(category_uuid)
				.build())
			.map(saved -> true)
			.onErrorReturn(false);
	}
}
