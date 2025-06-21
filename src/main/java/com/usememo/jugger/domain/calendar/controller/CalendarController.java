package com.usememo.jugger.domain.calendar.controller;

import java.time.Instant;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import com.usememo.jugger.global.security.CustomOAuth2User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/calendar")
@Tag(name = "캘린더 API", description = "캘린더 API에 대한 설명입니다.")
@RequiredArgsConstructor
public class CalendarController {

	private final CalendarService calendarService;

	@Operation(summary = "[POST] 일정등록")
	@PostMapping
	public Mono<ResponseEntity<Calendar>> postCalendar(@RequestBody PostCalendarDto postCalendarDto,
		@AuthenticationPrincipal
		CustomOAuth2User customOAuth2User) {
		return calendarService.postCalendar(postCalendarDto, customOAuth2User)
			.map(savedCalendar -> ResponseEntity
				.status(HttpStatus.CREATED)
				.body(savedCalendar));
	}

	@Operation(summary = "[GET] 일정조회")
	@GetMapping
	public Mono<ResponseEntity<List<GetCalendarDto>>> getCalendar(
		@RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
		@RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end, @AuthenticationPrincipal
	CustomOAuth2User customOAuth2User) {
		return calendarService.getCalendar(start, end, customOAuth2User).collectList()
			.map(list -> ResponseEntity.ok().body(list));

	}
}
