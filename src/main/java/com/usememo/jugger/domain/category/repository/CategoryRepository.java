package com.usememo.jugger.domain.category.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.usememo.jugger.domain.category.entity.Category;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CategoryRepository extends ReactiveMongoRepository<Category, String> {
	Mono<Category> findByNameAndUserUuid(String name,String userUuid);

	Mono<Category> findByUuid(String uuid);

	Flux<Category> findAllByUserUuid(String userUuid);

	Mono<Void> deleteByUuid(String categoryId);

	Mono<Void> deleteByUserUuid(String userId);
}
