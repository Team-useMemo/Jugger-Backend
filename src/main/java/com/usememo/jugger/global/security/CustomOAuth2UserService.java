package com.usememo.jugger.global.security;

import com.usememo.jugger.domain.user.entity.User;
import com.usememo.jugger.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultReactiveOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultReactiveOAuth2UserService {

	private final UserRepository userRepository;

	@Override
	public Mono<OAuth2User> loadUser(OAuth2UserRequest userRequest) {
		return super.loadUser(userRequest)
			.flatMap(oAuth2User -> {
				Map<String, Object> attributes = oAuth2User.getAttributes();
				String email = (String) attributes.get("email");
				String name = (String) attributes.get("name");
				String domain = userRequest.getClientRegistration().getRegistrationId();

				return userRepository.findByEmail(email)
					.switchIfEmpty(
						userRepository.save(User.builder()
							.email(email)
							.name(name)
							.domain(domain)
							.terms(new User.Terms())
							.build())
					)
					.map(user -> new CustomOAuth2User(attributes, user.getUuid()));
			});
	}
}
