package com.usememo.jugger.domain.category.service;

import com.mongodb.client.result.UpdateResult;
import com.usememo.jugger.domain.category.dto.GetRecentCategoryDto;
import com.usememo.jugger.domain.category.dto.PostCategoryDto;
import com.usememo.jugger.domain.category.entity.Category;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CategoryService {

	Mono<Category> createCategory(PostCategoryDto postCategoryDto);

	Flux<GetRecentCategoryDto> getRecentCategories();

	Mono<UpdateResult> pinCategory(String categoryId, boolean isPinned);
}

