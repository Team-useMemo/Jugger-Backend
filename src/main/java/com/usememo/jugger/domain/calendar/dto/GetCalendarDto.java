package com.usememo.jugger.domain.calendar.dto;

import java.time.Instant;

import lombok.Builder;
import lombok.Data;
import lombok.Setter;

@Builder
@Data
public class GetCalendarDto {
	@Setter
	private String chatId;
	private String calendarId;
	private Instant startDateTime;
	private Instant endDateTime;
	private String categoryId;
	private String categoryColor;
	private String title;
	private String place;
	private Instant alarm;
	private String description;
}
