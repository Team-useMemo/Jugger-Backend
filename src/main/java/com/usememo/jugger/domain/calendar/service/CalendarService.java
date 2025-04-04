package com.usememo.jugger.domain.calendar.service;

import com.usememo.jugger.domain.calendar.dto.PostCalendarDto;
import com.usememo.jugger.domain.calendar.entity.Calendar;

import reactor.core.publisher.Mono;

public interface CalendarService {

	// NOTE: V1용 API
	Mono<Calendar> postCalendar(PostCalendarDto postCalendarDto);
}
