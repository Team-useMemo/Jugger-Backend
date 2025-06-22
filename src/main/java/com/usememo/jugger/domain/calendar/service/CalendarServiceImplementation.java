package com.usememo.jugger.domain.calendar.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.usememo.jugger.domain.calendar.dto.GetCalendarDto;
import com.usememo.jugger.domain.calendar.dto.PostCalendarDto;
import com.usememo.jugger.domain.calendar.entity.Calendar;
import com.usememo.jugger.domain.calendar.repository.CalendarRepository;
import com.usememo.jugger.domain.category.repository.CategoryRepository;
import com.usememo.jugger.domain.chat.entity.Chat;
import com.usememo.jugger.domain.chat.repository.ChatRepository;
import com.usememo.jugger.global.security.CustomOAuth2User;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CalendarServiceImplementation implements CalendarService {

	private final CalendarRepository calendarRepository;
	private final ChatRepository chatRepository;

	private final CategoryRepository categoryRepository;

	@Override
	public Mono<Calendar> postCalendar(PostCalendarDto postCalendarDto,
		CustomOAuth2User customOAuth2User) {

		String calendarUuid = UUID.randomUUID().toString();
		Calendar calendar = Calendar.builder()
			.categoryUuid(postCalendarDto.getCategoryId())
			.title(postCalendarDto.getName())
			.uuid(calendarUuid)
			.userUuid(customOAuth2User.getUserId())
			.startDateTime(postCalendarDto.getStartTime())
			.endDateTime(postCalendarDto.getEndTime())
			.alarm(postCalendarDto.getAlarm())
			.description(postCalendarDto.getDescription())
			.place(postCalendarDto.getPlace())
			.build();

		Chat chat = Chat.builder()
			.uuid(UUID.randomUUID().toString())
			.userUuid(customOAuth2User.getUserId())
			.categoryUuid(postCalendarDto.getCategoryId())
			.data(postCalendarDto.getName())
			.refs(Chat.Refs.builder().calendarUuid(calendarUuid).build())
			.build();

		return calendarRepository.save(calendar)
			.flatMap(savedCalendar ->
				chatRepository.save(chat).thenReturn(savedCalendar)
			);
	}

	@Override
	public Flux<GetCalendarDto> getCalendar(Instant start, Instant end,
		CustomOAuth2User customOAuth2User) {
		return calendarRepository.findByUserUuidAndStartDateTimeBetween(customOAuth2User.getUserId(), start, end)
			.flatMap(calendar ->
				categoryRepository.findByUuid(calendar.getCategoryUuid())
					.map(category -> GetCalendarDto.builder()
						.categoryId(calendar.getCategoryUuid())
						.categoryColor(category.getColor())
						.title(calendar.getTitle())
						.startDateTime(calendar.getStartDateTime())
						.endDateTime(calendar.getEndDateTime())
						.alarm(calendar.getAlarm())
						.place(calendar.getPlace())
						.description(calendar.getDescription())
						.build()
					)
			);
	}
}
