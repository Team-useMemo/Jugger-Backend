package com.usememo.jugger.domain.user.service;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.usememo.jugger.domain.user.dto.WithdrawalRequest;
import com.usememo.jugger.domain.user.entity.WithdrawalReason;
import com.usememo.jugger.domain.user.repository.UserRepository;
import com.usememo.jugger.domain.user.repository.WithdrawalReasonRepository;
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
	private final WithdrawalReasonRepository withdrawalReasonRepository;

	public Mono<Void> deleteUser(String userId, WithdrawalRequest withdrawalRequest) {
		Mono<Void> saveReason = withdrawalReasonRepository.save(
			WithdrawalReason.builder()
				.userUuid(userId)
				.reasonCode(withdrawalRequest.reasonCode())
				.reasonDetail(withdrawalRequest.reasonDetail())
				.withdrawAt(Instant.now())
				.build()
		).then();

		Mono<Void> softDelete = userRepository.findById(userId)
			.switchIfEmpty(Mono.error(new BaseException(ErrorCode.USER_NOT_FOUND)))
			.flatMap(user -> {
				user.setDeleted(true);
				return userRepository.save(user);
			})
			.then(refreshTokenRepository.deleteByUserId(userId));

		return saveReason.then(softDelete);
	}
}
