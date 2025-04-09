package com.usememo.jugger.global.s3.service;

import io.awspring.cloud.s3.S3Template;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.InputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.usememo.jugger.domain.photo.dto.PhotoDto;
import com.usememo.jugger.domain.photo.entity.Photo;
import com.usememo.jugger.domain.photo.repository.PhotoRepository;

class S3ServiceImplementationTest {

	@Test
	@DisplayName("S3 업로드 테스트")
	void uploadFile_shouldReturnUrl_whenSuccessful() {
		// given
		S3Template s3Template = mock(S3Template.class);
		PhotoRepository photoRepository = mock(PhotoRepository.class);
		S3ServiceImplementation s3ServiceImplementation = new S3ServiceImplementation(s3Template, photoRepository);

		String testBucketName = "jugger-bucket";
		ReflectionTestUtils.setField(s3ServiceImplementation, "bucketName", testBucketName);

		FilePart filePart = mock(FilePart.class);
		when(filePart.filename()).thenReturn("test-image.jpg");

		String category_uuid = "jugger-category";
		String user_uuid = "user1";

		DataBuffer dataBuffer = new DefaultDataBufferFactory().wrap("fake-image-content".getBytes());
		when(filePart.content()).thenReturn(Flux.just(dataBuffer));

		when(s3Template.upload(eq(testBucketName), any(String.class), any(InputStream.class)))
			.thenReturn(null); // 실제 업로드는 void or null로 처리됨

		// ✅ DB 저장 mocking 추가!
		Photo mockPhoto = mock(Photo.class);
		when(photoRepository.save(any(Photo.class))).thenReturn(Mono.just(mockPhoto));

		// when & then
		StepVerifier.create(s3ServiceImplementation.uploadFile(
				PhotoDto.builder()
					.category_uuid(category_uuid)
					.user_uuid(user_uuid)
					.filePart(filePart)
					.build()))
			.assertNext(url -> {
				assert url.startsWith("https://" + testBucketName + ".s3.ap-northeast-2.amazonaws.com/");
				assert url.endsWith(".jpg");
			})
			.verifyComplete();

		verify(s3Template, times(1)).upload(eq(testBucketName), any(String.class), any(InputStream.class));
		verify(photoRepository, times(1)).save(any(Photo.class));
	}
}
