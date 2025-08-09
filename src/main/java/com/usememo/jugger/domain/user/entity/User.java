package com.usememo.jugger.domain.user.entity;

import java.text.DateFormat;
import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Document(collection = "users")
@Getter
@ToString
public class User {
	@Id
	private String uuid;

	@Setter
	private String name;
	private String email;
	private String domain;
	@Setter
	private LocalDate birthDay;
	@Setter
	private Gender gender;

	private boolean isDeleted = false;

	public void setDeleted(boolean deleted) {
		isDeleted = deleted;
	}

	@Setter
	private Terms terms;
	@Setter
	private UserStatus status;

	@Data
	public static class Terms {
		private boolean ageOver;
		private boolean privacyPolicy;
		private boolean termsOfService;
		private boolean marketing;
		private boolean termsOfAd;
	}

	public User() {
	}

	@Builder
	public User(String uuid, String name, String email, String domain, Terms terms,UserStatus status, Gender gender,LocalDate birthDay) {
		this.uuid = uuid;
		this.name = name;
		this.email = email;
		this.domain = domain;
		this.isDeleted = false;
		this.terms = terms;
		this.status = status;
		this.gender = gender;
		this.birthDay = birthDay;
	}

}
