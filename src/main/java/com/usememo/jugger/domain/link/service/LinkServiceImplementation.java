package com.usememo.jugger.domain.link.service;

import static java.awt.SystemColor.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.usememo.jugger.domain.category.repository.CategoryRepository;
import com.usememo.jugger.domain.chat.entity.Chat;
import com.usememo.jugger.domain.chat.repository.ChatRepository;
import com.usememo.jugger.domain.link.dto.GetLinkDto;
import com.usememo.jugger.domain.link.dto.LinkListResponse;
import com.usememo.jugger.domain.link.dto.LinkRequest;
import com.usememo.jugger.domain.link.dto.LinkResponse;
import com.usememo.jugger.domain.link.dto.LinkUpdateRequest;
import com.usememo.jugger.domain.link.entity.Link;
import com.usememo.jugger.domain.link.repository.LinkRepository;
import com.usememo.jugger.global.exception.BaseException;
import com.usememo.jugger.global.exception.ErrorCode;
import com.usememo.jugger.global.security.CustomOAuth2User;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class LinkServiceImplementation implements LinkService {
	private final LinkRepository linkRepository;

	private final CategoryRepository categoryRepository;
	private final ReactiveMongoTemplate reactiveMongoTemplate;
	private final ChatRepository chatRepository;

	@Override
	public Mono<List<GetLinkDto>> getLinks(Instant before, int page, int size, CustomOAuth2User customOAuth2User, String categoryUuid) {
		String userId = customOAuth2User.getUserId();

		return categoryRepository.findByUuid(categoryUuid)
			.flatMap(category -> {


				Query query = new Query()
					.addCriteria(Criteria.where("userUuid").is(userId))
					.addCriteria(Criteria.where("categoryUuid").is(categoryUuid))
					.addCriteria(Criteria.where("created_at").lt(before))
					.with(Sort.by(Sort.Direction.DESC, "created_at"))
					.skip((long) page * size)
					.limit(size);

				return reactiveMongoTemplate.find(query, Link.class)
					.map(link -> GetLinkDto.LinkData.builder()
						.linkId(link.getId())
						.link(link.getUrl())
						.build())
					.collectList()
					.map(linkDataList -> GetLinkDto.builder()
						.categoryId(categoryUuid)
						.linkData(linkDataList)
						.build());
			})
			.map(List::of);
	}

	@Override
	public  Mono<List<LinkListResponse>> getLinksNoCategory(Instant before, int page, int size, CustomOAuth2User customOAuth2User){
		String userId = customOAuth2User.getUserId();
				Query query = new Query()
					.addCriteria(Criteria.where("userUuid").is(userId))
					.addCriteria(Criteria.where("created_at").lt(before))
					.with(Sort.by(Sort.Direction.DESC, "created_at"))
					.skip((long) page * size)
					.limit(size);

				return reactiveMongoTemplate.find(query, Link.class)
					.map(link -> LinkListResponse.LinkData.builder()
						.categoryId(link.getCategoryUuid())
						.linkId(link.getId())
						.link(link.getUrl())
						.build())
					.collectList()
					.map(linkDataList -> LinkListResponse.builder()
						.linkData(linkDataList)
						.build()).map(List::of);
	}


	@Override
	public Mono<LinkResponse> postLink(CustomOAuth2User customOAuth2User, LinkRequest request){
		String userId = customOAuth2User.getUserId();
		String categoryId = request.categoryId();
		String chatId = UUID.randomUUID().toString();

		return 	categoryRepository.findByUuid(categoryId)
				.switchIfEmpty(Mono.error(new BaseException(ErrorCode.NO_CATEGORY)))
				.then(chatRepository.save(Chat.builder()
					.uuid(chatId)
					.userUuid(customOAuth2User.getUserId())
					.categoryUuid(categoryId)
					.data(request.link())
					.build())).then(
						linkRepository.save(
							Link.builder()
								.uuid(UUID.randomUUID().toString())
								.categoryUuid(categoryId)
								.url(request.link())
								.userUuid(userId)
								.chatUuid(chatId)
								.build()))
				.thenReturn(new LinkResponse(201,"링크가 등록되었습니다."));
	}

	@Override
	public Mono<LinkResponse> updateLink(CustomOAuth2User customOAuth2User, LinkUpdateRequest request){
		String userId = customOAuth2User.getUserId();
		String categoryId = request.categoryId();

		return categoryRepository.findByUuid(categoryId)
			.switchIfEmpty(Mono.error(new BaseException(ErrorCode.NO_CATEGORY)))
			.then(linkRepository.findByUuidAndUserUuid(request.linkId(),userId)
			).flatMap(link -> {
				link.setCategoryUuid(categoryId);
				link.setUrl(request.url());
				return linkRepository.save(link);
				}
			).thenReturn(new LinkResponse(200,"링크를 수정하였습니다."));
	}



}
