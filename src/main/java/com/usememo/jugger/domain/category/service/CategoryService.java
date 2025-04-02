package com.usememo.jugger.domain.category.service;

import com.usememo.jugger.domain.category.dto.PostCategoryDto;
import com.usememo.jugger.domain.category.entity.Category;

import reactor.core.publisher.Mono;

public interface CategoryService {
	Mono<Category> createCategory(PostCategoryDto postCategoryDto);
}
