package com.usememo.jugger.domain.link.service;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.usememo.jugger.domain.category.entity.Category;
import com.usememo.jugger.domain.category.repository.CategoryRepository;
import com.usememo.jugger.domain.link.dto.GetLinkDto;
import com.usememo.jugger.domain.link.entity.Link;
import com.usememo.jugger.domain.link.repository.LinkRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class LinkServiceImplementationTest {

	@Mock
	private CategoryRepository categoryRepository;

	@Mock
	private LinkRepository linkRepository;

	@InjectMocks
	private LinkServiceImplementation linkService;

	@Test
	@DisplayName(value = "get link test")
	void getLinks() {
		String categoryUuid = "category-123";
		String categoryName = "My Category";

		Category category = Category.builder()
			.uuid(categoryUuid)
			.name(categoryName)
			.build();

		Link link = Link.builder()
			.uuid("link-1")
			.url("http://example.com")
			.categoryUuid(categoryUuid)
			.caption("Example caption")
			.build();

		when(categoryRepository.findByUuid(categoryUuid))
			.thenReturn(Mono.just(category));

		when(linkRepository.findByCategoryUuid(categoryUuid))
			.thenReturn(Flux.just(link));

		StepVerifier.create(linkService.getLinks(categoryUuid))
			.expectNextMatches(list -> {
				if (list.size() != 1)
					return false;
				GetLinkDto getLinkDto = list.get(0);
				return getLinkDto.getCategoryUuid().equals(categoryUuid)
					&& getLinkDto.getCategoryName().equals(categoryName)
					&& getLinkDto.getLinkData().get(0).getLink().equals(link.getUrl())
					&& getLinkDto.getLinkData().get(0).getCaption().equals(link.getCaption());
			})
			.verifyComplete();
	}

}