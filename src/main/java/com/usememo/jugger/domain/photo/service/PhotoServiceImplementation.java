package com.usememo.jugger.domain.photo.service;

import org.springframework.stereotype.Service;

import com.usememo.jugger.domain.category.repository.CategoryRepository;
import com.usememo.jugger.domain.photo.dto.GetPhotoDto;
import com.usememo.jugger.domain.photo.dto.GetPhotoRequestDto;
import com.usememo.jugger.domain.photo.repository.PhotoRepository;
import com.usememo.jugger.global.security.CustomOAuth2User;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class PhotoServiceImplementation implements PhotoService {

	private final PhotoRepository photoRepository;
	private final CategoryRepository categoryRepository;

	public Flux<GetPhotoDto> getPhotoDto(GetPhotoRequestDto photoRequestDto, CustomOAuth2User customOAuth2User) {
		return categoryRepository.findByUuid(photoRequestDto.getCategoryUuid())
			.flatMapMany(category ->
				photoRepository
					.findByUserUuidAndCategoryUuid(customOAuth2User.getUserId(), photoRequestDto.getCategoryUuid())
					.map(photo -> GetPhotoDto.builder()
						.url(photo.getUrl())
						.categoryName(category.getName())
						.timestamp(photo.getCreatedAt())
						.build())
			);
	}

}
