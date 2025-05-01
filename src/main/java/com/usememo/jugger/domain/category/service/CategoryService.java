package com.usememo.jugger.domain.category.service;

import com.mongodb.client.result.UpdateResult;
import com.usememo.jugger.domain.category.dto.GetRecentCategoryDto;
import com.usememo.jugger.domain.category.dto.PostCategoryDto;
import com.usememo.jugger.domain.category.entity.Category;
import com.usememo.jugger.global.security.CustomOAuth2User;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CategoryService {

	Mono<Category> createCategory(PostCategoryDto postCategoryDto, CustomOAuth2User customOAuth2User);

	Flux<GetRecentCategoryDto> getRecentCategories(CustomOAuth2User customOAuth2User);

	Mono<UpdateResult> pinCategory(String categoryId, boolean isPinned);
}

