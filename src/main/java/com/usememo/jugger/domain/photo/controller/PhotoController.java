package com.usememo.jugger.domain.photo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.usememo.jugger.domain.photo.dto.GetPhotoDto;
import com.usememo.jugger.domain.photo.dto.GetPhotoRequestDto;
import com.usememo.jugger.domain.photo.service.PhotoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "사진 API", description = "사진 API에 대한 설명입니다.")
@RequiredArgsConstructor
public class PhotoController {

	private final PhotoService photoService;

	@Operation(summary = "[GET] 사진 조회")
	@GetMapping("/photos")
	public Flux<GetPhotoDto> getPhotos(
		@RequestParam String category_uuid) {
		return photoService.getPhotoDto(GetPhotoRequestDto.builder()
			.categoryUuid(category_uuid)
			.build());
	}
}
