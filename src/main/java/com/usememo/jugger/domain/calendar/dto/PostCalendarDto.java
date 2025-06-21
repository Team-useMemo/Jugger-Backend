package com.usememo.jugger.domain.calendar.dto;

import java.time.Instant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostCalendarDto {
	private String name;
	private Instant startTime;
	private Instant endTime;
	private String categoryId;
	private String place;
	private Instant alarm;
	private String description;
}
