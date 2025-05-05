package com.usememo.jugger.domain.user.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.usememo.jugger.domain.user.entity.User;

import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveMongoRepository<User,String> {
	Mono<User> findByEmailAndDomain(String email, String domain);

}
