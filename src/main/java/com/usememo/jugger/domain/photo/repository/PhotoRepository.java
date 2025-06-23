package com.usememo.jugger.domain.photo.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.usememo.jugger.domain.photo.entity.Photo;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PhotoRepository extends ReactiveMongoRepository<Photo, String> {
	Flux<Photo> findByUserUuidAndCategoryUuid(String userUuid, String categoryUuid);

	Mono<Void> deleteByUserUuid(String userId);
}
