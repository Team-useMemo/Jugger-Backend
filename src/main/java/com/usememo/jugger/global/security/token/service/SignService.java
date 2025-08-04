package com.usememo.jugger.global.security.token.service;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.usememo.jugger.domain.user.entity.User;
import com.usememo.jugger.domain.user.entity.UserStatus;
import com.usememo.jugger.domain.user.repository.UserRepository;
import com.usememo.jugger.global.exception.BaseException;
import com.usememo.jugger.global.exception.ErrorCode;
import com.usememo.jugger.global.exception.KakaoException;
import com.usememo.jugger.global.security.JwtTokenProvider;
import com.usememo.jugger.global.security.token.domain.SignUpRequest;

import com.usememo.jugger.global.security.token.domain.token.NewTokenResponse;
import com.usememo.jugger.global.security.token.domain.token.TokenResponse;
import com.usememo.jugger.global.security.token.domain.userResponse.KakaoUserResponse;
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


	public Mono<User> saveOrFindUser(Map<String, Object> userInfo,String domain) {
		String email = (String)userInfo.get("email");
		String name = (String)userInfo.get("name");
		if (email == null) {
			return Mono.error(new BaseException(ErrorCode.GOOGLE_NO_EMAIL));
		}
		if (name == null) {
			return Mono.error(new BaseException(ErrorCode.GOOGLE_NO_NAME));
		}

		return userRepository.findByEmailAndDomain(email, domain)
			.switchIfEmpty(Mono.defer(() -> {
				String uuid = UUID.randomUUID().toString();

				User user = User.builder()
					.uuid(uuid)
					.name(name)
					.email(email)
					.domain(domain)
					.status(UserStatus.PENDING)
					.build();

				userRepository.save(user);

				return Mono.error(new KakaoException(ErrorCode.USER_NOT_FOUND,
					Map.of("email", email, "nickname", name)));
			}));
	}

	public Mono<User> saveOrFindUserKakao(KakaoUserResponse response,String domain) {
		String email = response.getKakao_account().getEmail();
		String name = response.getProperties().getNickname();
		if (email == null) {
			return Mono.error(new BaseException(ErrorCode.GOOGLE_NO_EMAIL));
		}
		if (name == null) {
			return Mono.error(new BaseException(ErrorCode.GOOGLE_NO_NAME));
		}

		return userRepository.findByEmailAndDomain(email, domain)
			.switchIfEmpty(
				Mono.defer(() -> {
					String uuid = UUID.randomUUID().toString();

					User user = User.builder()
						.uuid(uuid)
						.name(name)
						.email(email)
						.domain(domain)
						.status(UserStatus.PENDING)
						.build();

					return userRepository.save(user)
						.flatMap(savedUser ->
							Mono.error(new KakaoException(ErrorCode.USER_NOT_FOUND,
								Map.of("email", email, "nickname", name))));
				})
			);
	}




}
