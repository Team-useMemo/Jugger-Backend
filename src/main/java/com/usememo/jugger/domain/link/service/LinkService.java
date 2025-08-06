package com.usememo.jugger.domain.link.service;

import java.time.Instant;
import java.util.List;

import com.usememo.jugger.domain.link.dto.GetLinkDto;
import com.usememo.jugger.domain.link.dto.LinkListResponse;
import com.usememo.jugger.domain.link.dto.LinkRequest;
import com.usememo.jugger.domain.link.dto.LinkResponse;
import com.usememo.jugger.domain.link.dto.LinkUpdateRequest;
import com.usememo.jugger.global.response.BaseResponse;
import com.usememo.jugger.global.security.CustomOAuth2User;

import reactor.core.publisher.Mono;

public interface LinkService {
	Mono<List<GetLinkDto>> getLinks(Instant before, int page, int size, CustomOAuth2User customOAuth2User, String categoryUuid);

	Mono<List<LinkListResponse>> getLinksNoCategory(Instant before, int page , int size, CustomOAuth2User customOAuth2User);

	Mono<LinkResponse> postLink(CustomOAuth2User customOAuth2User, LinkRequest request);

	Mono<LinkResponse> updateLink(CustomOAuth2User customOAuth2User, LinkUpdateRequest request);

	Mono<BaseResponse> deleteByLinkId(CustomOAuth2User customOAuth2User, String linkId);
}
