package com.usememo.jugger.domain.photo.service;

import com.usememo.jugger.domain.photo.dto.GetPhotoDto;
import com.usememo.jugger.domain.photo.dto.GetPhotoRequestDto;
import com.usememo.jugger.domain.photo.entity.Photo;
import com.usememo.jugger.domain.photo.repository.PhotoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.Mockito.*;

class PhotoServiceImplementationTest {

	@Test
	@DisplayName("Photo 목록을 조회하고 Dto로 변환")
	void getPhotoDto_shouldReturnFluxOfGetPhotoDto() {
		// given
		PhotoRepository photoRepository = mock(PhotoRepository.class);
		PhotoServiceImplementation photoService = new PhotoServiceImplementation(photoRepository);

		String userUuid = "user1";
		String categoryUuid = "여행";
		String url1 = "https://s3.amazon.com/photo1.jpg";
		String url2 = "https://s3.amazon.com/photo2.jpg";

		Photo photo1 = Photo.builder().url(url1).build();
		Photo photo2 = Photo.builder().url(url2).build();

		when(photoRepository.findByUserUuidAndCategoryUuid(userUuid, categoryUuid))
			.thenReturn(Flux.just(photo1, photo2));

		GetPhotoRequestDto requestDto = GetPhotoRequestDto.builder()
			.userUuid(userUuid)
			.categoryUuid(categoryUuid)
			.build();

		// when
		Flux<GetPhotoDto> result = photoService.getPhotoDto(requestDto);

		// then
		StepVerifier.create(result)
			.expectNext(GetPhotoDto.builder().url(url1).build())
			.expectNext(GetPhotoDto.builder().url(url2).build())
			.verifyComplete();

		verify(photoRepository, times(1)).findByUserUuidAndCategoryUuid(userUuid, categoryUuid);
	}
}
