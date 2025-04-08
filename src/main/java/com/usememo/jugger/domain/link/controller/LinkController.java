package com.usememo.jugger.domain.link.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.usememo.jugger.domain.link.dto.GetLinkDto;
import com.usememo.jugger.domain.link.service.LinkService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/links")
@RequiredArgsConstructor
public class LinkController {

	private final LinkService linkService;

	@GetMapping
	public Mono<ResponseEntity<List<GetLinkDto>>> getLinks(@RequestParam("categoryId") String categoryUuid) {
		return linkService.getLinks(categoryUuid)
			.map(list -> ResponseEntity.ok().body(list));

	}
}
