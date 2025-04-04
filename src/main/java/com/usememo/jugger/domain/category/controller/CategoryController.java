package com.usememo.jugger.domain.category.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.usememo.jugger.domain.category.dto.PostCategoryDto;
import com.usememo.jugger.domain.category.entity.Category;
import com.usememo.jugger.domain.category.service.CategoryService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

	private final CategoryService categoryService;

	@PostMapping
	public Mono<ResponseEntity<Category>> createCategory(@RequestBody PostCategoryDto postCategoryDto) {
		return categoryService.createCategory(postCategoryDto)
			.map(savedCategory -> ResponseEntity
				.status(HttpStatus.CREATED)
				.body(savedCategory));
	}
}