package com.usememo.jugger.domain.link.service;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.usememo.jugger.domain.category.repository.CategoryRepository;
import com.usememo.jugger.domain.link.dto.GetLinkDto;
import com.usememo.jugger.domain.link.entity.Link;
import com.usememo.jugger.domain.link.repository.LinkRepository;
import com.usememo.jugger.global.security.CustomOAuth2User;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class LinkServiceImplementation implements LinkService {
	private final LinkRepository linkRepository;

	private final CategoryRepository categoryRepository;
	private final ReactiveMongoTemplate reactiveMongoTemplate;

	@Override
	public Mono<List<GetLinkDto>> getLinks(Instant before, int page, int size, CustomOAuth2User customOAuth2User, String categoryUuid) {
		String userId = customOAuth2User.getUserId();

		return categoryRepository.findByUuid(categoryUuid)
			.flatMap(category -> {
				String categoryName = category.getName();

				Query query = new Query()
					.addCriteria(Criteria.where("user_uuid").is(userId))
					.addCriteria(Criteria.where("category_uuid").is(categoryUuid))
					.addCriteria(Criteria.where("created_at").lt(before))
					.with(Sort.by(Sort.Direction.DESC, "created_at"))
					.skip((long) page * size)
					.limit(size);

				return reactiveMongoTemplate.find(query, Link.class)
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
