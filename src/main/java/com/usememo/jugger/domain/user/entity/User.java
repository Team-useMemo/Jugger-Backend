package com.usememo.jugger.domain.user.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Document(collection = "users")
@AllArgsConstructor
@Builder
public class User {
	@Id
	private String uuid;
	private String name;
	private String email;
	private String domain;

	private Terms terms;

	@Data
	public static class Terms {
		private boolean termsOfService;
		private boolean privacyPolicy;
		private boolean marketing;
	}
}
