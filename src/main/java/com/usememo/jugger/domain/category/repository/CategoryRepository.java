package com.usememo.jugger.domain.category.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.usememo.jugger.domain.category.entity.Category;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CategoryRepository extends ReactiveMongoRepository<Category, String> {
	Mono<Category> findByName(String name);

	Mono<Category> findByUuid(String uuid);

	Flux<Category> findAllByUserUuidOrderByUpdatedAtDesc(String userId);

}
