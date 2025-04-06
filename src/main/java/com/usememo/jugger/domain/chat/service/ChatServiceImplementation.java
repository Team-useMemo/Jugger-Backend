package com.usememo.jugger.domain.chat.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.usememo.jugger.domain.calendar.entity.Calendar;
import com.usememo.jugger.domain.calendar.repository.CalendarRepository;
import com.usememo.jugger.domain.category.repository.CategoryRepository;
import com.usememo.jugger.domain.chat.dto.GetChatByCategoryDto;
import com.usememo.jugger.domain.chat.dto.GetChatTypeDto;
import com.usememo.jugger.domain.chat.dto.PostChatDto;
import com.usememo.jugger.domain.chat.entity.Chat;
import com.usememo.jugger.domain.chat.entity.ChatType;
import com.usememo.jugger.domain.chat.repository.ChatRepository;
import com.usememo.jugger.domain.link.entity.Link;
import com.usememo.jugger.domain.link.repository.LinkRepository;
import com.usememo.jugger.global.exception.chat.CategoryNullException;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ChatServiceImplementation implements ChatService {

	private final CalendarRepository calendarRepository;
	private final LinkRepository linkRepository;
	private final CategoryRepository categoryRepository;

	private final ChatRepository chatRepository;

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

	@Override
	public Mono<Void> postChat(PostChatDto postChatDto) {

		return Mono.justOrEmpty(postChatDto.getCategoryUuid())
			.switchIfEmpty(Mono.error(new CategoryNullException()))
			.flatMap(categoryUuid -> {
				String chatUuid = UUID.randomUUID().toString();

				Chat chat = Chat.builder()
					.uuid(chatUuid)
					.userUuid("123456789a")
					.categoryUuid(categoryUuid)
					.data(postChatDto.getText())
					.build();

				return chatRepository.save(chat).then();
			});

	}

	@Override
	public Mono<List<GetChatByCategoryDto>> getChatsBefore(Instant before, int page, int size) {
		int skip = page * size;

		return chatRepository.findByCreatedAtBeforeOrderByCreatedAtDesc(before)
			.skip(skip)
			.take(size)
			.collectList()
			.flatMap(this::groupByCategory);
	}

	@Override
	public Mono<List<GetChatByCategoryDto>> getChatsAfter(Instant before, int page, int size) {
		int skip = page * size;

		return chatRepository.findByCreatedAtBeforeOrderByCreatedAtDesc(before)
			.skip(skip)
			.take(size)
			.collectList()
			.flatMap(this::groupByCategory);
	}

	private Mono<Void> saveCalendar(PostChatDto dto) {
		Calendar calendar = Calendar.builder()
			.uuid(UUID.randomUUID().toString())
			.userUuid("12345678")
			.title("title")
			.startDateTime(Instant.from(LocalDateTime.now()))
			.endDateTime(Instant.from(LocalDateTime.now()))
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

	private Mono<List<GetChatByCategoryDto>> groupByCategory(List<Chat> chats) {
		Map<String, List<Chat>> grouped = chats.stream()
			.collect(Collectors.groupingBy(Chat::getCategoryUuid));

		// NOTE: Flux로 묶어서 카테고리마다 처리
		return Flux.fromIterable(grouped.entrySet())
			.flatMap(entry -> {
				String categoryId = entry.getKey();
				List<Chat> chatList = entry.getValue();

				return categoryRepository.findById(categoryId)
					.map(category -> {
						List<GetChatByCategoryDto.ChatItem> chatItems = chatList.stream()
							.map(chat -> GetChatByCategoryDto.ChatItem.builder()
								.data(chat.getData())
								.calendar(
									chat.getRefs() != null && chat.getRefs().getCalendarUuid() != null ?
										chat.getRefs() : null)
								.photo(
									chat.getRefs() != null && chat.getRefs().getPhotoUuid() != null ? chat.getRefs() :
										null)
								.link(
									chat.getRefs() != null && chat.getRefs().getLinkUuid() != null ? chat.getRefs() :
										null)
								.timestamp(chat.getCreatedAt())
								.build())
							.toList();

						return GetChatByCategoryDto.builder()
							.categoryId(categoryId)
							.categoryName(category.getName())
							.categoryColor(category.getColor())
							.chatItems(chatItems)
							.build();
					});
			})
			.collectList();
	}

}

