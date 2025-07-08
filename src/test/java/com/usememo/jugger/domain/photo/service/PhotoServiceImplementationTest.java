package com.usememo.jugger.domain.photo.service;

import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import com.usememo.jugger.domain.category.entity.Category;
import com.usememo.jugger.domain.category.repository.CategoryRepository;
import com.usememo.jugger.domain.chat.repository.ChatRepository;
import com.usememo.jugger.domain.photo.dto.PhotoResponse;
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
		ReactiveMongoTemplate reactiveMongoTemplate = mock(ReactiveMongoTemplate.class);
		ChatRepository chatRepository = mock(ChatRepository.class);
		PhotoServiceImplementation photoService = new PhotoServiceImplementation(photoRepository,reactiveMongoTemplate,chatRepository);

		String userUuid = "123456789a";
		String categoryUuid = "여행";

		String url1 = "https://s3.amazon.com/photo1.jpg";
		String url2 = "https://s3.amazon.com/photo2.jpg";

		Photo photo1 = Photo.builder().url(url1).build();
		Photo photo2 = Photo.builder().url(url2).build();

		String categoryId = "123";
		Category mockCategoty = Category.builder().uuid(categoryId).build();

		when(categoryRepository.findByUuid(categoryUuid)).thenReturn(Mono.just(mockCategoty));

		when(photoRepository.findByUserUuidAndCategoryUuid(userUuid, categoryUuid))
			.thenReturn(Flux.just(photo1, photo2));

		GetPhotoRequestDto requestDto = GetPhotoRequestDto.builder()
			.categoryId(categoryUuid)
			.build();

		// when
		Flux<PhotoResponse> result = photoService.getPhotoDto(requestDto, customOAuth2User);

		// then
		StepVerifier.create(result)
			.expectNext(PhotoResponse.builder().url(url1).categoryId(categoryId).build())
			.expectNext(PhotoResponse.builder().url(url2).categoryId(categoryId).build())
			.verifyComplete();

		verify(photoRepository, times(1)).findByUserUuidAndCategoryUuid(userUuid, categoryUuid);
	}
}
