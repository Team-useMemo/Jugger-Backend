package com.usememo.jugger.domain.calendar.entity;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Document(collection = "calendars")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class Calendar {

	@Id
	private String uuid;
	private String userUuid;
	@Setter
	private String title;
	@Setter
	private Instant startDateTime;
	@Setter
	private Instant endDateTime;
	@Setter
	private String categoryUuid;
	@Setter
	private String place;
	@Setter
	private Instant alarm;
	@Setter
	private String description;

	public Calendar() {

	}
}
