package com.usememo.jugger.domain.category.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.usememo.jugger.domain.category.dto.PostCategoryDto;
import com.usememo.jugger.domain.category.entity.Category;
import com.usememo.jugger.domain.category.repository.CategoryRepository;
import com.usememo.jugger.domain.chat.service.ChatService;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class CategoryServiceImplementationTest {
	private CategoryRepository categoryRepository;
	private CategoryService categoryService;

	private ChatService chatService;

	@BeforeEach
	void setUp() {
		categoryRepository = mock(CategoryRepository.class);
		categoryService = new CategoryServiceImplementation(categoryRepository, chatService);
	}

	@Test
	@DisplayName("카테고리 생성")
	void createCategory() {

		PostCategoryDto postCategoryDto = PostCategoryDto.builder()
			.name("운동")
			.color("#FF0000")
			.build();

		ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);

		Category savedCategory = Category.builder()
			.uuid("mock-uuid")
			.name("운동")
			.color("#FF0000")
			.build();

		when(categoryRepository.save(any(Category.class)))
			.thenReturn(Mono.just(savedCategory));

		when(categoryRepository.findByName("운동"))
			.thenReturn(Mono.empty());

		Mono<Category> result = categoryService.createCategory(postCategoryDto);

		StepVerifier.create(result)
			.assertNext(category -> {
				assertThat(category.getName()).isEqualTo("운동");
				assertThat(category.getColor()).isEqualTo("#FF0000");
			})
			.verifyComplete();

		verify(categoryRepository, times(1)).save(captor.capture());

		Category captured = captor.getValue();
		assertThat(captured.getUuid()).isNotNull();
		assertThat(captured.getName()).isEqualTo("운동");
		assertThat(captured.getColor()).isEqualTo("#FF0000");
	}

}