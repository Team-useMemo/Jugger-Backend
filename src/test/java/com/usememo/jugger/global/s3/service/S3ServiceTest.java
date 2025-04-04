package com.usememo.jugger.global.s3.service;

import io.awspring.cloud.s3.S3Template;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.InputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class S3ServiceTest {

	@Test
	@DisplayName("S3 업로드 테스트")
	void uploadFile_shouldReturnUrl_whenSuccessful() {
		// given
		S3Template s3Template = mock(S3Template.class);
		S3Service s3Service = new S3Service(s3Template);

		FilePart filePart = mock(FilePart.class);
		when(filePart.filename()).thenReturn("test-image.jpg");

		DataBuffer dataBuffer = new DefaultDataBufferFactory().wrap("fake-image-content".getBytes());
		when(filePart.content()).thenReturn(Flux.just(dataBuffer));

		when(s3Template.upload(eq("jugger-bucket"), any(String.class), any(InputStream.class)))
			.thenReturn(null);

		// when & then
		StepVerifier.create(s3Service.uploadFile(filePart))
			.assertNext(url -> {
				assert url.startsWith("https://jugger-bucket.s3.ap-northeast-2.amazonaws.com/");
				assert url.endsWith(".jpg");
			})
			.verifyComplete();

		verify(s3Template, times(1))
			.upload(eq("jugger-bucket"), any(String.class), any(InputStream.class));
	}
}
