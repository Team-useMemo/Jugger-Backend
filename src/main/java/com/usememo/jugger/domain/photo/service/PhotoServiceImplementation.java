package com.usememo.jugger.domain.photo.service;

import org.springframework.stereotype.Service;

import com.usememo.jugger.domain.photo.dto.GetPhotoDto;
import com.usememo.jugger.domain.photo.dto.GetPhotoRequestDto;
import com.usememo.jugger.domain.photo.repository.PhotoRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class PhotoServiceImplementation implements  PhotoService{

	private final PhotoRepository photoRepository;

	public Flux<GetPhotoDto> getPhotoDto(GetPhotoRequestDto photoRequestDto){
		return photoRepository
			.findByUserUuidAndCategoryUuid(photoRequestDto.getUserUuid(), photoRequestDto.getCategoryUuid())
			.map(photo -> GetPhotoDto.builder()
				.url(photo.getUrl())
				.build());
	}

}
