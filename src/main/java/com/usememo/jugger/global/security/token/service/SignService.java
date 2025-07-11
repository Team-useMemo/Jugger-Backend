package com.usememo.jugger.global.security.token.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.usememo.jugger.domain.user.entity.User;
import com.usememo.jugger.domain.user.entity.UserStatus;
import com.usememo.jugger.domain.user.repository.UserRepository;
import com.usememo.jugger.global.exception.BaseException;
import com.usememo.jugger.global.exception.ErrorCode;
import com.usememo.jugger.global.security.JwtTokenProvider;
import com.usememo.jugger.global.security.token.domain.SignUpRequest;
import com.usememo.jugger.global.security.token.domain.NewTokenResponse;
import com.usememo.jugger.global.security.token.domain.TokenResponse;
import com.usememo.jugger.global.security.token.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class SignService {

	private final UserRepository userRepository;
	private final JwtTokenProvider jwtTokenProvider;
	private final RefreshTokenRepository refreshTokenRepository;

	public Mono<TokenResponse> signUp(SignUpRequest signUpRequest) {
		String email = signUpRequest.email();
		String domain = signUpRequest.domain();
		String name = signUpRequest.name();

		return userRepository.findByEmailAndDomainAndName(email, domain, name)
			.flatMap(existingUser -> {
					User.Terms terms = new User.Terms();
					terms.setMarketing(signUpRequest.terms().isMarketing());
					terms.setPrivacyPolicy(signUpRequest.terms().isPrivacyPolicy());
					terms.setTermsOfService(signUpRequest.terms().isTermsOfService());

					existingUser.setTerms(terms);
					existingUser.setStatus(UserStatus.SUCCESS);

					return userRepository.save(existingUser)
						.flatMap(savedUser -> jwtTokenProvider.createTokenBundle(savedUser.getUuid()));
				}
			).switchIfEmpty(
				Mono.error(new BaseException(ErrorCode.FAIL_SIGNUP)));
	}


	public Mono<Void> userLogOut(String refreshToken) {
		String userId;
		userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
		if (userId == null) {
			throw new BaseException(ErrorCode.NO_LOGOUT_USER);
		}
		return refreshTokenRepository.findByUserId(userId)
			.switchIfEmpty(Mono.error(new BaseException(ErrorCode.NO_LOGOUT_USER)))
			.flatMap(foundToken -> refreshTokenRepository.deleteByUserId(userId));
	}


	public Mono<ResponseEntity<NewTokenResponse>> giveNewToken(String refreshToken) {
		return refreshTokenRepository.findByToken(refreshToken)
			.flatMap(savedToken -> {
				if (!jwtTokenProvider.validateToken(savedToken.getToken())) {
					return Mono.error(new BaseException(ErrorCode.NO_REFRESH_TOKEN));
				}
				String userId = jwtTokenProvider.getUserIdFromToken(savedToken.getToken());
				return userRepository.findById(userId)
					.map(user -> {
						String newAccessToken = jwtTokenProvider.createAccessToken(userId);
						return ResponseEntity.ok().body(new NewTokenResponse(newAccessToken));
					});
			})
			.switchIfEmpty(Mono.error(new BaseException(ErrorCode.NO_REFRESH_TOKEN)));
	}



}
