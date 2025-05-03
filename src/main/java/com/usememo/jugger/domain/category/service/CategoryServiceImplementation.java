package com.usememo.jugger.domain.category.service;

import java.util.UUID;

import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

import com.mongodb.client.result.UpdateResult;
import com.usememo.jugger.domain.category.dto.GetRecentCategoryDto;
import com.usememo.jugger.domain.category.dto.PostCategoryDto;
import com.usememo.jugger.domain.category.entity.Category;
import com.usememo.jugger.domain.category.repository.CategoryRepository;
import com.usememo.jugger.domain.chat.service.ChatService;
import com.usememo.jugger.global.exception.category.CategoryExistException;
import com.usememo.jugger.global.security.CustomOAuth2User;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CategoryServiceImplementation implements CategoryService {
	private final CategoryRepository categoryRepository;
	private final ChatService chatService;

	private final ReactiveMongoTemplate reactiveMongoTemplate;

	public Mono<Category> createCategory(PostCategoryDto dto,
		@AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
		return categoryRepository.findByName(dto.getName())
			.flatMap(existing -> Mono.<Category>error(new CategoryExistException()))
			.switchIfEmpty(Mono.defer(() -> {
				Category newCategory = Category.builder()
					.uuid(UUID.randomUUID().toString())
					.name(dto.getName())
					.color(dto.getColor())
					.userUuid(customOAuth2User.getUserId())
					.build();
				return categoryRepository.save(newCategory);
			}));
	}

	@Override
	public Flux<GetRecentCategoryDto> getRecentCategories(CustomOAuth2User customOAuth2User) {
		return categoryRepository.findAllByUserUuidOrderByUpdatedAtDesc(customOAuth2User.getUserId())
			.flatMap(category ->
				chatService.getLatestChatByCategoryId(category.getUuid())
					.switchIfEmpty(Mono.justOrEmpty(null)) // null-safe
					.map(chat -> GetRecentCategoryDto.builder()
						.uuid(category.getUuid())
						.name(category.getName())
						.color(category.getColor())
						.isPinned(category.getIsPinned())
						.updateAt(category.getUpdatedAt())
						.recentMessage(chat != null ? chat.getData() : null)
						.build()));
	}

	@Override
	public Mono<UpdateResult> pinCategory(String categoryId, boolean isPinned) {
		Query query = Query.query(Criteria.where("uuid").is(categoryId));
		Update update = new Update().set("isPinned", isPinned);
		return reactiveMongoTemplate.updateFirst(query, update, Category.class);

	}

}
