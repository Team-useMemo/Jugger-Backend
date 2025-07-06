package com.usememo.jugger.domain.user.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;

@Document(collection = "users")
@Getter
@ToString
public class User {
	@Id
	private String uuid;
	private String name;
	private String email;
	private String domain;
	private boolean isDeleted = false;

	public void setDeleted(boolean deleted) {
		isDeleted = deleted;
	}

	private Terms terms;

	@Data
	public static class Terms {
		private boolean termsOfService;
		private boolean privacyPolicy;
		private boolean marketing;
	}

	public User() {
	}

	@Builder
	public User(String uuid, String name, String email, String domain, Terms terms) {
		this.uuid = uuid;
		this.name = name;
		this.email = email;
		this.domain = domain;
		this.isDeleted = false;
		this.terms = terms;
	}

}
