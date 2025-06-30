package com.usememo.jugger.domain.photo.service;

import java.time.Instant;

import com.usememo.jugger.domain.photo.dto.PhotoResponse;
import com.usememo.jugger.domain.photo.dto.GetPhotoRequestDto;
import com.usememo.jugger.domain.photo.dto.PhotoUpdateRequest;
import com.usememo.jugger.global.response.BaseResponse;
import com.usememo.jugger.global.security.CustomOAuth2User;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PhotoService {
	Flux<PhotoResponse> getPhotoDto(GetPhotoRequestDto photoRequestDto, CustomOAuth2User customOAuth2User);

	Flux<PhotoResponse> getPhotoDuration(Instant before,int page, int size,CustomOAuth2User customOAuth2User);

	Flux<PhotoResponse> getPhotoCategoryAndDuration(String categoryId, Instant before, int page, int size, CustomOAuth2User customOAuth2User);

	Mono<BaseResponse> updatePhotoDescription(CustomOAuth2User customOAuth2User, PhotoUpdateRequest request);
}
