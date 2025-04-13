package com.usememo.jugger.domain.photo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.usememo.jugger.domain.photo.dto.GetPhotoDto;
import com.usememo.jugger.domain.photo.dto.GetPhotoRequestDto;
import com.usememo.jugger.domain.photo.repository.PhotoRepository;
import com.usememo.jugger.domain.photo.service.PhotoService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PhotoController {

	private final PhotoService photoService;

	@GetMapping("/photos")
	public Flux<GetPhotoDto> getPhotos(
		@RequestParam String category_uuid){
		return photoService.getPhotoDto(GetPhotoRequestDto.builder()
			.categoryUuid(category_uuid)
			.build());
	}
}
