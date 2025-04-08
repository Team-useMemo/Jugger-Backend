package com.usememo.jugger.domain.calendar.controller;

import java.time.Instant;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.usememo.jugger.domain.calendar.dto.GetCalendarDto;
import com.usememo.jugger.domain.calendar.dto.PostCalendarDto;
import com.usememo.jugger.domain.calendar.entity.Calendar;
import com.usememo.jugger.domain.calendar.service.CalendarService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/calendar")
@RequiredArgsConstructor
public class CalendarController {

	private final CalendarService calendarService;

	@PostMapping
	public Mono<ResponseEntity<Calendar>> postCalendar(@RequestBody PostCalendarDto postCalendarDto) {
		return calendarService.postCalendar(postCalendarDto)
			.map(savedCalendar -> ResponseEntity
				.status(HttpStatus.CREATED)
				.body(savedCalendar));
	}

	@GetMapping
	public Mono<ResponseEntity<List<GetCalendarDto>>> getCalendar(
		@RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
		@RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) {
		return calendarService.getCalendar(start, end).collectList()
			.map(list -> ResponseEntity.ok().body(list));

	}
}
