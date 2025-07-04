package com.usememo.jugger.domain.user.service;

import org.springframework.stereotype.Service;

import com.usememo.jugger.domain.user.repository.UserRepository;
import com.usememo.jugger.global.exception.BaseException;
import com.usememo.jugger.global.exception.ErrorCode;
import com.usememo.jugger.global.security.token.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserServiceImplementation implements UserService {
	private final RefreshTokenRepository refreshTokenRepository;
	private final UserRepository userRepository;

	public Mono<Void> deleteUser(String userId) {
		return userRepository.findById(userId)
			.switchIfEmpty(Mono.error(new BaseException(ErrorCode.KAKAO_USER_NOT_FOUND)))
			.flatMap(user -> {
				user.setDeleted(true);
				return userRepository.save(user);
			})
			.then(refreshTokenRepository.deleteByUserId(userId))
			.then();
	}
}
