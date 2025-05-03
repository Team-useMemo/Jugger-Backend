package com.usememo.jugger.domain.photo.service;

import com.usememo.jugger.domain.photo.dto.GetPhotoDto;
import com.usememo.jugger.domain.photo.dto.GetPhotoRequestDto;
import com.usememo.jugger.global.security.CustomOAuth2User;

import reactor.core.publisher.Flux;

public interface PhotoService {
	Flux<GetPhotoDto> getPhotoDto(GetPhotoRequestDto photoRequestDto, CustomOAuth2User customOAuth2User);
}
