package com.usememo.jugger.domain.photo.service;

import java.time.Instant;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.usememo.jugger.domain.category.entity.Category;
import com.usememo.jugger.domain.category.repository.CategoryRepository;
import com.usememo.jugger.domain.photo.dto.GetPhotoDto;
import com.usememo.jugger.domain.photo.dto.GetPhotoRequestDto;
import com.usememo.jugger.domain.photo.entity.Photo;
import com.usememo.jugger.domain.photo.repository.PhotoRepository;
import com.usememo.jugger.global.security.CustomOAuth2User;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class PhotoServiceImplementation implements PhotoService {

	private final PhotoRepository photoRepository;
	private final CategoryRepository categoryRepository;
	private final ReactiveMongoTemplate reactiveMongoTemplate;

	@Override
	public Flux<GetPhotoDto> getPhotoDto(GetPhotoRequestDto photoRequestDto, CustomOAuth2User customOAuth2User) {
		return categoryRepository.findByUuid(photoRequestDto.getCategoryId())
			.flatMapMany(category ->
				photoRepository
					.findByUserUuidAndCategoryUuid(customOAuth2User.getUserId(), photoRequestDto.getCategoryId())
					.map(photo -> GetPhotoDto.builder()
						.url(photo.getUrl())
						.categoryName(category.getName())
						.timestamp(photo.getCreatedAt())
						.build())
			);
	}
	@Override
	public Flux<GetPhotoDto> getPhotoDuration(Instant before, int page, int size, CustomOAuth2User customOAuth2User){
		String userId = customOAuth2User.getUserId();

		Query query = new Query()
			.addCriteria(Criteria.where("userUuid").is(userId))
			.addCriteria(Criteria.where("updatedAt").lt(before))
			.with(Sort.by(Sort.Direction.DESC, "updatedAt"))
			.skip((long) page * size)
			.limit(size);

		return reactiveMongoTemplate.findAll(Category.class)
			.collectMap(Category::getUuid, Category::getName)
			.flatMapMany(categoryMap ->
				reactiveMongoTemplate.find(query, Photo.class)
					.map(photo -> GetPhotoDto.builder()
						.url(photo.getUrl())
						.categoryName(categoryMap.getOrDefault(photo.getCategoryUuid(), "미지정"))
						.timestamp(photo.getUpdatedAt())
						.build())
			);
	}

}
