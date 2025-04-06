package com.usememo.jugger.global.s3.service;

import io.awspring.cloud.s3.S3Template;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.InputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class S3ServiceImplementationTest {

	@Test
	@DisplayName("S3 업로드 테스트")
	void uploadFile_shouldReturnUrl_whenSuccessful() {
		// given
		S3Template s3Template = mock(S3Template.class);
		S3ServiceImplementation s3ServiceImplementation = new S3ServiceImplementation(s3Template);

		String testBucketName = "jugger-bucket";
		ReflectionTestUtils.setField(s3ServiceImplementation, "bucketName", testBucketName);

		FilePart filePart = mock(FilePart.class);
		when(filePart.filename()).thenReturn("test-image.jpg");

		DataBuffer dataBuffer = new DefaultDataBufferFactory().wrap("fake-image-content".getBytes());
		when(filePart.content()).thenReturn(Flux.just(dataBuffer));

		when(s3Template.upload(eq(testBucketName), any(String.class), any(InputStream.class)))
			.thenReturn(null);

		// when & then
		StepVerifier.create(s3ServiceImplementation.uploadFile(filePart))
			.assertNext(url -> {
				assert url.startsWith("https://" + testBucketName + ".s3.ap-northeast-2.amazonaws.com/");
				assert url.endsWith(".jpg");
			})
			.verifyComplete();

		verify(s3Template, times(1))
			.upload(eq(testBucketName), any(String.class), any(InputStream.class));
	}
}
