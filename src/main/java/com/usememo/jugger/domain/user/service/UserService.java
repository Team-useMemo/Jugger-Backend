package com.usememo.jugger.domain.user.service;

import reactor.core.publisher.Mono;

public interface UserService {

	public Mono<Void> deleteUser(String userId);
}
