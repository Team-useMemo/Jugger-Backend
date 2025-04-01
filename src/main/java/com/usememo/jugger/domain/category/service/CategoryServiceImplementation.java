package com.usememo.jugger.domain.category.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.usememo.jugger.domain.category.dto.PostCategoryDto;
import com.usememo.jugger.domain.category.entity.Category;
import com.usememo.jugger.domain.category.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CategoryServiceImplementation implements CategoryService {
	private final CategoryRepository categoryRepository;

	public Mono<Category> createCategory(PostCategoryDto postCategoryDto) {
		Category category = Category.builder()
			.uuid(UUID.randomUUID().toString())
			.name(postCategoryDto.getName())
			.color(postCategoryDto.getColor())
			.build();

		return categoryRepository.save(category);
	}
}
