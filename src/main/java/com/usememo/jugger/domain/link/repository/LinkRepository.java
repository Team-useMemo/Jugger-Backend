package com.usememo.jugger.domain.link.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.usememo.jugger.domain.link.entity.Link;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LinkRepository extends ReactiveMongoRepository<Link, String> {

	Flux<Link> findByCategoryUuidAndUserUuid(String categoryUuid,String userUuid);

	Mono<Void> deleteByUserUuid(String userId);
}
