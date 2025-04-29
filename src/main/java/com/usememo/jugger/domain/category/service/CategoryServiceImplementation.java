package com.usememo.jugger.domain.category.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.usememo.jugger.domain.category.dto.GetRecentCategoryDto;
import com.usememo.jugger.domain.category.dto.PostCategoryDto;
import com.usememo.jugger.domain.category.entity.Category;
import com.usememo.jugger.domain.category.repository.CategoryRepository;
import com.usememo.jugger.domain.chat.service.ChatService;
import com.usememo.jugger.global.exception.category.CategoryExistException;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CategoryServiceImplementation implements CategoryService {
	private final CategoryRepository categoryRepository;
	private final ChatService chatService;

	public Mono<Category> createCategory(PostCategoryDto dto) {
		return categoryRepository.findByName(dto.getName())
			.flatMap(existing -> Mono.<Category>error(new CategoryExistException()))
			.switchIfEmpty(Mono.defer(() -> {
				Category newCategory = Category.builder()
					.uuid(UUID.randomUUID().toString())
					.name(dto.getName())
					.color(dto.getColor())
					.userUuid("123456789a")
					.build();
				return categoryRepository.save(newCategory);
			}));
	}

	@Override
	public Flux<GetRecentCategoryDto> getRecentCategories() {
		return categoryRepository.findAllByUserUuidOrderByUpdatedAtDesc("123456789a")
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

}
