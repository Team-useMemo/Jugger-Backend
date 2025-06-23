package com.usememo.jugger.global.s3.service;

import java.io.InputStream;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Service;

import com.usememo.jugger.domain.chat.entity.Chat;
import com.usememo.jugger.domain.chat.repository.ChatRepository;
import com.usememo.jugger.domain.photo.dto.PhotoDto;
import com.usememo.jugger.domain.photo.entity.Photo;
import com.usememo.jugger.domain.photo.repository.PhotoRepository;
import com.usememo.jugger.global.exception.s3.S3UploadException;

import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class S3ServiceImplementation implements S3Service {

	private final S3Template s3Template;
	private final PhotoRepository photoRepository;
	private final ChatRepository chatRepository;
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

					return savePhoto(saveUrl, photoDto.getUserId(), photoDto.getCategoryId())
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

	private Mono<Boolean> savePhoto(String saveUrl, String userUuid, String categoryUuid) {
		Photo photo = Photo.builder()
			.photoUuid(UUID.randomUUID().toString())
			.url(saveUrl)
			.userUuid(userUuid)
			.categoryUuid(categoryUuid)
			.build();

		return photoRepository.save(photo)
			.flatMap(savedPhoto -> {
				Chat chat = Chat.builder()
					.uuid(UUID.randomUUID().toString())
					.userUuid(userUuid)
					.categoryUuid(categoryUuid)
					.data(null)
					.refs(Chat.Refs.builder()
						.photoUuid(savedPhoto.getPhotoUuid())
						.build())
					.build();

				return chatRepository.save(chat)
					.thenReturn(true);  // Photo && Chat 모두 성공 → true
			})
			.onErrorResume(e -> Mono.just(false));  // Photo || Chat 실패 → false
	}

}
