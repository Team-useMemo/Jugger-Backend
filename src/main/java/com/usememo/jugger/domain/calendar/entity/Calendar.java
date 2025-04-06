package com.usememo.jugger.domain.calendar.entity;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Document(collection = "calendars")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class Calendar {
	@Id
	private String uuid;
	private String userUuid;
	private String title;
	private Instant startDateTime;
	private Instant endDateTime;

	private String categoryUuid;

}
