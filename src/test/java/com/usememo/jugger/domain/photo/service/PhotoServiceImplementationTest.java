package com.usememo.jugger.domain.photo.service;

import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.usememo.jugger.domain.category.entity.Category;
import com.usememo.jugger.domain.category.repository.CategoryRepository;
import com.usememo.jugger.domain.photo.dto.GetPhotoDto;
import com.usememo.jugger.domain.photo.dto.GetPhotoRequestDto;
import com.usememo.jugger.domain.photo.entity.Photo;
import com.usememo.jugger.domain.photo.repository.PhotoRepository;
import com.usememo.jugger.global.security.CustomOAuth2User;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class PhotoServiceImplementationTest {

	@Test
	@DisplayName("Photo 목록을 조회하고 Dto로 변환")
	void getPhotoDto_shouldReturnFluxOfGetPhotoDto() {

		Map<String, Object> attributes = new HashMap<>();
		CustomOAuth2User customOAuth2User = new CustomOAuth2User(attributes, "123456789a");
		// given
		PhotoRepository photoRepository = mock(PhotoRepository.class);
		CategoryRepository categoryRepository = mock(CategoryRepository.class);
		PhotoServiceImplementation photoService = new PhotoServiceImplementation(photoRepository, categoryRepository);

		String userUuid = "123456789a";
		String categoryUuid = "여행";

		String url1 = "https://s3.amazon.com/photo1.jpg";
		String url2 = "https://s3.amazon.com/photo2.jpg";

		Photo photo1 = Photo.builder().url(url1).build();
		Photo photo2 = Photo.builder().url(url2).build();

		String categoryName = "여행";
		Category mockCategoty = Category.builder().name(categoryName).build();

		when(categoryRepository.findByUuid(categoryUuid)).thenReturn(Mono.just(mockCategoty));

		when(photoRepository.findByUserUuidAndCategoryUuid(userUuid, categoryUuid))
			.thenReturn(Flux.just(photo1, photo2));

		GetPhotoRequestDto requestDto = GetPhotoRequestDto.builder()
			.categoryUuid(categoryUuid)
			.build();

		// when
		Flux<GetPhotoDto> result = photoService.getPhotoDto(requestDto, customOAuth2User);

		// then
		StepVerifier.create(result)
			.expectNext(GetPhotoDto.builder().url(url1).categoryName(categoryName).build())
			.expectNext(GetPhotoDto.builder().url(url2).categoryName(categoryName).build())
			.verifyComplete();

		verify(photoRepository, times(1)).findByUserUuidAndCategoryUuid(userUuid, categoryUuid);
	}
}
