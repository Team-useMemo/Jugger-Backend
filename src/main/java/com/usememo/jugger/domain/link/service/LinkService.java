package com.usememo.jugger.domain.link.service;

import java.util.List;

import com.usememo.jugger.domain.link.dto.GetLinkDto;

import reactor.core.publisher.Mono;

public interface LinkService {
	Mono<List<GetLinkDto>> getLinks(String categoryUuid);
}
