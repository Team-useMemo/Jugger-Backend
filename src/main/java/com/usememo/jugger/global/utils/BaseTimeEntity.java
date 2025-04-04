package com.usememo.jugger.global.utils;

import java.time.ZonedDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Getter;

@Getter
public abstract class BaseTimeEntity {

	@CreatedDate
	@Field("created_at")
	private ZonedDateTime createdAt;

	@LastModifiedDate
	@Field("updated_at")
	private ZonedDateTime updatedAt;
}