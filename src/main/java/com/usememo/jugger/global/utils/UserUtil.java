package com.usememo.jugger.global.utils;

import org.springframework.stereotype.Component;

import com.usememo.jugger.domain.user.entity.User;
import com.usememo.jugger.domain.user.repository.UserRepository;
import com.usememo.jugger.global.exception.BaseException;
import com.usememo.jugger.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UserUtil {

	private final UserRepository userRepository;


	public Mono<User> findUserValid(String userId){

		return userRepository.findById(userId)
			.flatMap(user -> {
				if (!user.isDeleted()) {
					return Mono.just(user);
				}
				return Mono.error(new BaseException(ErrorCode.NO_USER));
			})
			.switchIfEmpty(Mono.error(new BaseException(ErrorCode.NO_USER)));

	}

}
