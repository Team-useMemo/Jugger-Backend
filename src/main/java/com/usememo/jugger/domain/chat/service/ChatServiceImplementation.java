package com.usememo.jugger.domain.chat.service;

import java.time.LocalDateTime;
import java.util.UUID;

import com.usememo.jugger.domain.calendar.entity.Calendar;
import com.usememo.jugger.domain.calendar.repository.CalendarRepository;
import com.usememo.jugger.domain.chat.dto.GetChatTypeDto;
import com.usememo.jugger.domain.chat.dto.PostChatDto;
import com.usememo.jugger.domain.chat.entity.ChatType;
import com.usememo.jugger.domain.link.entity.Link;
import com.usememo.jugger.domain.link.repository.LinkRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ChatServiceImplementation implements ChatService {

	private final CalendarRepository calendarRepository;
	private final LinkRepository linkRepository;

	@Override
	public Mono<Void> processChat(PostChatDto postChatDto) {
		// TODO: AI 서버 결과는 실제 배포 시 교체 예정
		GetChatTypeDto getChatTypeDto = GetChatTypeDto.builder()
			.chatType(ChatType.CALENDAR)
			.build();

		return switch (getChatTypeDto.getChatType()) {
			case CALENDAR -> saveCalendar(postChatDto);
			case LINK -> saveLink(postChatDto);
			default -> Mono.empty();
		};
	}

	private Mono<Void> saveCalendar(PostChatDto dto) {
		Calendar calendar = Calendar.builder()
			.uuid(UUID.randomUUID().toString())
			.userUuid("12345678")
			.title("title")
			.startDateTime(LocalDateTime.now())
			.endDateTime(LocalDateTime.now())
			.categoryUuid(dto.getCategoryUuid())
			.build();

		return calendarRepository.save(calendar).then();
	}

	private Mono<Void> saveLink(PostChatDto dto) {
		Link link = Link.builder()
			.uuid(UUID.randomUUID().toString())
			.userUuid("12345678")
			.categoryUuid(dto.getCategoryUuid())
			.build();

		return linkRepository.save(link).then();
	}
}

