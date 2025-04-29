package com.usememo.jugger.domain.link.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.usememo.jugger.domain.link.dto.GetLinkDto;
import com.usememo.jugger.domain.link.service.LinkService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@Tag(name = "링크 API", description = "링크 API에 대한 설명입니다.")
@RequestMapping("/api/v1/links")
@RequiredArgsConstructor
public class LinkController {

	private final LinkService linkService;

	@Operation(summary = "[GET] 링크 조회")
	@GetMapping
	public Mono<ResponseEntity<List<GetLinkDto>>> getLinks(@RequestParam("categoryId") String categoryUuid) {
		return linkService.getLinks(categoryUuid)
			.map(list -> ResponseEntity.ok().body(list));

	}
}
