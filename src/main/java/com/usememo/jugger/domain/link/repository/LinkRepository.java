package com.usememo.jugger.domain.link.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.usememo.jugger.domain.link.entity.Link;

import reactor.core.publisher.Flux;

public interface LinkRepository extends ReactiveMongoRepository<Link, String> {

	Flux<Link> findByCategoryUuid(String categoryUuid);
}
