package com.usememo.jugger.domain.user.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;

@Document(collection = "users")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
@ToString
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
