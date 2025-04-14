package com.usememo.jugger.domain.calendar.service;

import static org.mockito.Mockito.*;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.usememo.jugger.domain.calendar.dto.PostCalendarDto;
import com.usememo.jugger.domain.calendar.entity.Calendar;
import com.usememo.jugger.domain.calendar.repository.CalendarRepository;
import com.usememo.jugger.domain.category.repository.CategoryRepository;
import com.usememo.jugger.domain.chat.entity.Chat;
import com.usememo.jugger.domain.chat.repository.ChatRepository;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class CalendarServiceImplementationTest {

	@Mock
	private CalendarRepository calendarRepository;

	private CalendarServiceImplementation calendarService;
	private ChatRepository chatRepository;
	private CategoryRepository categoryRepository;
	@BeforeEach
	void setUp() {
		chatRepository = mock(ChatRepository.class);
		calendarRepository = mock(CalendarRepository.class);
		categoryRepository = mock(CategoryRepository.class);
		calendarService = new CalendarServiceImplementation(calendarRepository, chatRepository,categoryRepository);
	}

	@Test
	@DisplayName("일정 입력 테스트")
	void postCalendar() {

		PostCalendarDto dto = PostCalendarDto.builder()
			.categoryId("category-123")
			.name("일정 테스트")
			.startTime(ZonedDateTime.now().toInstant())
			.endTime(Instant.from(ZonedDateTime.now().toInstant())).build();

		Calendar savedCalendar = Calendar.builder()
			.uuid("calendar-uuid-123")
			.categoryUuid(dto.getCategoryId())
			.startDateTime(dto.getStartTime())
			.endDateTime(dto.getEndTime())
			.build();

		Chat savedChat = Chat.builder()
			.uuid(UUID.randomUUID().toString())
			.userUuid("123456789a")
			.categoryUuid("category-123")
			.data("일정 테스트")
			.refs(Chat.Refs.builder().calendarUuid("category-123").build())
			.build();

		when(chatRepository.save(any(Chat.class)))
			.thenReturn(Mono.just(savedChat));
		
		when(calendarRepository.save(any(Calendar.class)))
			.thenReturn(Mono.just(savedCalendar));

		Mono<Calendar> result = calendarService.postCalendar(dto);

		StepVerifier.create(result)
			.expectNextMatches(calendar ->
				calendar.getCategoryUuid().equals(dto.getCategoryId()) &&
					calendar.getStartDateTime().equals(dto.getStartTime()) &&
					calendar.getEndDateTime().equals(dto.getEndTime())
			)
			.verifyComplete();

		verify(calendarRepository, times(1)).save(any(Calendar.class));
	}

}