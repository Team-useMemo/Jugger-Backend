package com.usememo.jugger.domain.calendar.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Document(collection = "calendars")
@AllArgsConstructor
@Builder
public class Calendar {
	@Id
	private String uuid;
	private String userUuid;

	private String title;
	private LocalDateTime timestamp;

	private Category category;

	@Data
	public static class Category {
		private String categoryUuid;
		private String name;
	}
}
