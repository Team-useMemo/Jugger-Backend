package com.usememo.jugger.domain.calendar.dto;

import java.time.Instant;

public record UpdateRequest(String calendarId, String categoryId, Instant start, Instant end, String title, String description, String place ,Instant alarm) {
}
