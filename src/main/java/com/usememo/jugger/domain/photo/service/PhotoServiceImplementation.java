package com.usememo.jugger.domain.photo.service;

import java.time.Instant;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.usememo.jugger.domain.photo.dto.GetPhotoRequestDto;
import com.usememo.jugger.domain.photo.dto.PhotoResponse;
import com.usememo.jugger.domain.photo.dto.PhotoUpdateRequest;
import com.usememo.jugger.domain.photo.entity.Photo;
import com.usememo.jugger.domain.photo.repository.PhotoRepository;
import com.usememo.jugger.global.exception.BaseException;
import com.usememo.jugger.global.exception.ErrorCode;
import com.usememo.jugger.global.response.BaseResponse;
import com.usememo.jugger.global.security.CustomOAuth2User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhotoServiceImplementation implements PhotoService {

	private final PhotoRepository photoRepository;
	private final ReactiveMongoTemplate reactiveMongoTemplate;

	@Override
	public Flux<PhotoResponse> getPhotoDto(GetPhotoRequestDto photoRequestDto, CustomOAuth2User customOAuth2User) {
		return photoRepository
			.findByUserUuidAndCategoryUuid(customOAuth2User.getUserId(), photoRequestDto.getCategoryId())
			.map(photo -> PhotoResponse.builder()
				.photoId(photo.getId())
				.url(photo.getUrl())
				.categoryId(photo.getCategoryUuid())
				.description(photo.getDescription())
				.timestamp(photo.getUpdatedAt())
				.build());
	}

	@Override
	public Flux<PhotoResponse> getPhotoDuration(Instant before, int page, int size, CustomOAuth2User customOAuth2User) {
		String userId = customOAuth2User.getUserId();

		Query query = new Query()
			.addCriteria(Criteria.where("user_uuid").is(userId))
			.addCriteria(Criteria.where("created_at").lt(before))
			.with(Sort.by(Sort.Direction.DESC, "created_at"))
			.skip((long)page * size)
			.limit(size);

		return reactiveMongoTemplate.find(query, Photo.class)
			.map(photo ->
				PhotoResponse.builder()
					.photoId(photo.getId())
					.url(photo.getUrl())
					.categoryId(photo.getCategoryUuid())
					.timestamp(photo.getUpdatedAt())
					.description(photo.getDescription())
					.build()
			);

	}

	@Override
	public Flux<PhotoResponse> getPhotoCategoryAndDuration(String categoryId, Instant before, int page, int size,
		CustomOAuth2User customOAuth2User) {
		String userId = customOAuth2User.getUserId();

		Query query = new Query()
			.addCriteria(Criteria.where("user_uuid").is(userId))
			.addCriteria(Criteria.where("category_uuid").is(categoryId))
			.addCriteria(Criteria.where("created_at").lt(before))
			.with(Sort.by(Sort.Direction.DESC, "created_at"))
			.skip((long)page * size)
			.limit(size);

		return reactiveMongoTemplate.find(query, Photo.class)
			.map(photo ->
				PhotoResponse.builder()
					.photoId(photo.getId())
					.url(photo.getUrl())
					.categoryId(photo.getCategoryUuid())
					.timestamp(photo.getUpdatedAt())
					.description(photo.getDescription())
					.build()
			);

	}

	@Override
	public Mono<BaseResponse> updatePhotoDescription(CustomOAuth2User customOAuth2User, PhotoUpdateRequest request) {
		Query query = Query.query(
			Criteria.where("uuid").is(request.photoId())
				.and("userUuid").is(customOAuth2User.getUserId())
		);

		Update update = new Update().set("description", request.description());

		return reactiveMongoTemplate.updateFirst(query, update, Photo.class)
			.flatMap(result -> {
				if (result.getMatchedCount() == 0) {
					return Mono.error(new BaseException(ErrorCode.NO_PHOTO));
				}
				return Mono.just(new BaseResponse(200, "사진을 수정하였습니다."));
			});

	}

}
