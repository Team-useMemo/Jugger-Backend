package com.usememo.jugger.domain.calendar.service;

import java.time.Instant;

import com.usememo.jugger.domain.calendar.dto.GetCalendarDto;
import com.usememo.jugger.domain.calendar.dto.PostCalendarDto;
import com.usememo.jugger.domain.calendar.entity.Calendar;
import com.usememo.jugger.global.security.CustomOAuth2User;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CalendarService {

	// NOTE: V1용 API
	Mono<Calendar> postCalendar(PostCalendarDto postCalendarDto, CustomOAuth2User customOAuth2User);

	Flux<GetCalendarDto> getCalendar(Instant start, Instant end, CustomOAuth2User customOAuth2User);
}
