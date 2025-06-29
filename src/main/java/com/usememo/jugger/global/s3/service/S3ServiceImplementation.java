package com.usememo.jugger.global.s3.service;

import java.io.InputStream;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import com.usememo.jugger.domain.chat.entity.Chat;
import com.usememo.jugger.domain.chat.repository.ChatRepository;
import com.usememo.jugger.domain.photo.dto.PhotoDto;
import com.usememo.jugger.domain.photo.entity.Photo;
import com.usememo.jugger.domain.photo.repository.PhotoRepository;
import com.usememo.jugger.global.exception.BaseException;
import com.usememo.jugger.global.exception.ErrorCode;
import com.usememo.jugger.global.exception.s3.S3UploadException;
import com.usememo.jugger.global.s3.dto.FilesUploadResponse;
import com.usememo.jugger.global.security.CustomOAuth2User;
import com.usememo.jugger.global.utils.BaseTimeEntity;

import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class S3ServiceImplementation extends BaseTimeEntity implements S3Service {

	private final S3Template s3Template;
	private final PhotoRepository photoRepository;
	private final ChatRepository chatRepository;
	@Value("${spring.cloud.aws.s3.bucket}")
	private String bucketName;

	public Mono<FilesUploadResponse> uploadFiles(Flux<FilePart> files, CustomOAuth2User customOAuth2User, String categoryId, String description){
		long MAX_COUNT = 5;

		String userId = customOAuth2User.getUserId();
		return files
			.index()
			.flatMap(file -> {
			long index = file.getT1();
			FilePart filePart = file.getT2();
			if(index >= MAX_COUNT){
				return Mono.error(new BaseException(ErrorCode.UPLOAD_LIMIT));
			}
			PhotoDto photoDto = PhotoDto.builder()
				.categoryId(categoryId)
				.filePart(filePart)
				.userId(userId)
				.description(description)
				.build();
			return uploadFile(photoDto);
		}).collectList()
			.map(urls ->  FilesUploadResponse.of(200,"이미지 업로드에 성공하였습니다.",urls));
	}


	public Mono<String> uploadFile(PhotoDto photoDto) {
		String originalFilename = photoDto.getFilePart().filename();
		String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
		String newFileName = UUID.randomUUID() + ext;

		return DataBufferUtils.join(photoDto.getFilePart().content())
			.flatMap(dataBuffer -> {
				try (InputStream inputStream = dataBuffer.asInputStream()) {
					s3Template.upload(bucketName, newFileName, inputStream);
					String saveUrl = "https://" + bucketName + ".s3.ap-northeast-2.amazonaws.com/" + newFileName;

					return savePhoto(saveUrl, photoDto.getUserId(), photoDto.getCategoryId(),photoDto.getDescription())
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

	private Mono<Boolean> savePhoto(String saveUrl, String userUuid, String categoryUuid,String description) {
		String realDes = description;

		if(description == null){
			realDes = "";
		}

		Photo photo = Photo.builder()
			.uuid(UUID.randomUUID().toString())
			.url(saveUrl)
			.userUuid(userUuid)
			.description(realDes)
			.categoryUuid(categoryUuid)
			.build();

		return photoRepository.save(photo)
			.flatMap(savedPhoto -> {
				Chat chat = Chat.builder()
					.uuid(UUID.randomUUID().toString())
					.userUuid(userUuid)
					.categoryUuid(categoryUuid)
					.data(savedPhoto.getDescription())
					.refs(Chat.Refs.builder()
						.photoUuid(savedPhoto.getUuid())
						.build())
					.build();

				return chatRepository.save(chat)
					.thenReturn(true);
			})
			.onErrorResume(e -> Mono.just(false));
	}

}
