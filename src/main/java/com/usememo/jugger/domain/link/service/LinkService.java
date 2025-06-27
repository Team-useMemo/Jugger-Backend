package com.usememo.jugger.domain.link.service;

import java.time.Instant;
import java.util.List;

import com.usememo.jugger.domain.link.dto.GetLinkDto;
import com.usememo.jugger.global.security.CustomOAuth2User;

import reactor.core.publisher.Mono;

public interface LinkService {
	Mono<List<GetLinkDto>> getLinks(Instant before, int page, int size, CustomOAuth2User customOAuth2User, String categoryUuid);
}
