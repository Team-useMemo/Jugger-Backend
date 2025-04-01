package com.usememo.jugger.domain.calendar.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Document(collection = "calendars")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Calendar {
	@Id
	private String uuid;
	private String userUuid;

	private String title;
	private LocalDateTime timestamp;

	private String categoryUuid;

}
