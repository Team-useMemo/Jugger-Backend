package com.usememo.jugger.domain.user.service;

import com.usememo.jugger.domain.user.dto.WithdrawalRequest;

import reactor.core.publisher.Mono;

public interface UserService {

	public Mono<Void> deleteUser(String userId, WithdrawalRequest withdrawalRequest);
}
