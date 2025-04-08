package com.usememo.jugger.domain.link.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.usememo.jugger.domain.category.repository.CategoryRepository;
import com.usememo.jugger.domain.link.dto.GetLinkDto;
import com.usememo.jugger.domain.link.repository.LinkRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class LinkServiceImplementation implements LinkService {
	private final LinkRepository linkRepository;

	private final CategoryRepository categoryRepository;

	@Override
	public Mono<List<GetLinkDto>> getLinks(String categoryUuid) {
		return categoryRepository.findByUuid(categoryUuid)
			.flatMap(category -> {
				String categoryName = category.getName();

				return linkRepository.findByCategoryUuid(categoryUuid)
					.map(link -> GetLinkDto.LinkData.builder()
						.caption(link.getCaption())
						.link(link.getUrl())
						.build())
					.collectList()
					.map(linkDataList -> GetLinkDto.builder()
						.categoryUuid(categoryUuid)
						.categoryName(categoryName)
						.linkData(linkDataList)
						.build());
			})
			.map(List::of);
	}

}
