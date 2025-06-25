package com.usememo.jugger.domain.category.service;

import java.util.UUID;

import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.mongodb.client.result.UpdateResult;
import com.usememo.jugger.domain.category.dto.GetRecentCategoryDto;
import com.usememo.jugger.domain.category.dto.PostCategoryDto;
import com.usememo.jugger.domain.category.dto.PostCategoryWithUuidDto;
import com.usememo.jugger.domain.category.dto.UpdateRequest;
import com.usememo.jugger.domain.category.dto.UpdateResponse;
import com.usememo.jugger.domain.category.entity.Category;
import com.usememo.jugger.domain.category.repository.CategoryRepository;
import com.usememo.jugger.domain.chat.entity.Chat;
import com.usememo.jugger.domain.chat.repository.ChatRepository;
import com.usememo.jugger.domain.chat.service.ChatService;
import com.usememo.jugger.global.exception.BaseException;
import com.usememo.jugger.global.exception.ErrorCode;
import com.usememo.jugger.global.exception.category.CategoryExistException;
import com.usememo.jugger.global.exception.chat.CategoryNullException;
import com.usememo.jugger.global.security.CustomOAuth2User;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CategoryServiceImplementation implements CategoryService {
	private final CategoryRepository categoryRepository;

	private final ChatRepository chatRepository;
	private final ChatService chatService;

	private final ReactiveMongoTemplate reactiveMongoTemplate;

	public Mono<Category> createCategory(PostCategoryDto dto, CustomOAuth2User customOAuth2User) {
		return categoryRepository.findByName(dto.getName())
			.flatMap(existing -> Mono.<Category>error(new CategoryExistException()))
			.switchIfEmpty(Mono.defer(() -> {
				Category newCategory = Category.builder()
					.uuid(UUID.randomUUID().toString())
					.name(dto.getName())
					.color(dto.getColor())
					.userUuid(customOAuth2User.getUserId())
					.isPinned(false)
					.build();
				return categoryRepository.save(newCategory);
			}));
	}

	@Override
	public Mono<Category> createCategoryWithUuid(PostCategoryWithUuidDto postCategoryWithUuidDto,
		CustomOAuth2User customOAuth2User) {
		return categoryRepository.findByUuid(postCategoryWithUuidDto.getCategoryUuid())
			.switchIfEmpty(Mono.error(new CategoryNullException()))
			.flatMap(category -> {
				category.setName(postCategoryWithUuidDto.getName());
				category.setColor(postCategoryWithUuidDto.getColor());
				category.setPinned(false);
				return categoryRepository.save(category);
			});
	}

	@Override
	public Flux<GetRecentCategoryDto> getRecentCategories(CustomOAuth2User customOAuth2User) {
		String userId = customOAuth2User.getUserId();

		return categoryRepository.findAllByUserUuidOrderByUpdatedAtDesc(userId)
			.flatMap(category ->
				chatService.getLatestChatByCategoryId(customOAuth2User,category.getUuid())
					.map(Chat::getData)
					.defaultIfEmpty("")
					.map(recentMessage -> GetRecentCategoryDto.builder()
						.uuid(category.getUuid())
						.name(category.getName())
						.color(category.getColor())
						.isPinned(category.getIsPinned())
						.updateAt(category.getUpdatedAt())
						.recentMessage(recentMessage)
						.build())
			);
	}

	@Override
	public Mono<UpdateResult> pinCategory(String categoryId, boolean isPinned) {
		Query query = Query.query(Criteria.where("uuid").is(categoryId));
		Update update = new Update().set("isPinned", isPinned);
		return reactiveMongoTemplate.updateFirst(query, update, Category.class);

	}

	@Override
	public Mono<Boolean> deleteCategory(String categoryId, CustomOAuth2User customOAuth2User) {
		return categoryRepository.findByUuid(categoryId)
			.switchIfEmpty(Mono.error(new BaseException(ErrorCode.NO_CATEGORY)))
			.flatMap(category -> {
				if (!category.getUserUuid().equals(customOAuth2User.getUserId())) {
					return Mono.error(new BaseException(ErrorCode.NO_AUTHORITY));
				}
				return categoryRepository.deleteByUuid(categoryId)
					.then(chatRepository.deleteByCategoryUuid(categoryId))
					.thenReturn(true);
			});
	}

	@Override
	public Mono<UpdateResponse> updateCategory(UpdateRequest updateRequest, CustomOAuth2User customOAuth2User) {
		return categoryRepository.findByUuid(updateRequest.categoryId())
			.switchIfEmpty(Mono.error(new BaseException(ErrorCode.NO_CATEGORY)))
			.flatMap(category -> {
				if (!category.getUserUuid().equals(customOAuth2User.getUserId())) {
					return Mono.error(new BaseException(ErrorCode.NO_AUTHORITY));
				}
				Query query = new Query(Criteria.where("uuid").is(updateRequest.categoryId()));

				Update update = new Update();
				if (updateRequest.newName() != null) {
					update.set("name", updateRequest.newName());
				}
				if (updateRequest.newColor() != null) {
					update.set("color", updateRequest.newColor());
				}

				return reactiveMongoTemplate.updateFirst(query, update, Category.class)
					.map(result -> new UpdateResponse(
						200,
						"카테고리 업데이트가 완료되었습니다."
					));
			});

	}


}
