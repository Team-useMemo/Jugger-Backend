package com.usememo.jugger.global.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {

	private final Map<String, Object> attributes;
	private final String uuid;

	public CustomOAuth2User(Map<String, Object> attributes, String uuid) {
		this.attributes = attributes;
		this.uuid = uuid;
	}

	public String getUserId() {
		return uuid;
	}

	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	@Override
	public String getName() {
		return uuid;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(() -> "ROLE_USER");
	}
}
