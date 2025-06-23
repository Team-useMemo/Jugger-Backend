package com.usememo.jugger.domain.link.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.usememo.jugger.domain.link.entity.Link;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LinkRepository extends ReactiveMongoRepository<Link, String> {

	Flux<Link> findByCategoryUuid(String categoryUuid);

	Mono<Void> deleteByUserUuid(String userId);
}
