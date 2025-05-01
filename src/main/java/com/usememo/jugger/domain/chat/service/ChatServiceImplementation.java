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
import com.usememo.jugger.domain.photo.entity.Photo;
import com.usememo.jugger.domain.photo.repository.PhotoRepository;
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
	private final PhotoRepository photoRepository;

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
				if (isLink(postChatDto.getText())) {
					return saveLinkChat(postChatDto, categoryUuid);
				} else {
					return saveTextChat(postChatDto, categoryUuid);
				}
			});
	}

	private Mono<Void> saveLinkChat(PostChatDto dto, String categoryUuid) {
		String chatUuid = UUID.randomUUID().toString();
		String linkUuid = UUID.randomUUID().toString();

		Link link = Link.builder()
			.uuid(linkUuid)
			.userUuid("123456789a")
			.categoryUuid(categoryUuid)
			.url(dto.getText())
			.build();

		Chat chat = Chat.builder()
			.uuid(chatUuid)
			.userUuid("123456789a")
			.categoryUuid(categoryUuid)
			.data(dto.getText())
			.refs(Chat.Refs.builder().linkUuid(linkUuid).build())
			.build();

		return linkRepository.save(link)
			.then(chatRepository.save(chat))
			.then(); // Mono<Void>
	}

	private Mono<Void> saveTextChat(PostChatDto dto, String categoryUuid) {
		String chatUuid = UUID.randomUUID().toString();

		Chat chat = Chat.builder()
			.uuid(chatUuid)
			.userUuid("123456789a")
			.categoryUuid(categoryUuid)
			.data(dto.getText())
			.build();

		return chatRepository.save(chat).then();
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
	public Mono<List<GetChatByCategoryDto>> getChatsAfter(Instant after, int page, int size) {
		int skip = page * size;

		return chatRepository.findByCreatedAtAfterOrderByCreatedAtDesc(after)
			.skip(skip)
			.take(size)
			.collectList()
			.flatMap(this::groupByCategory);
	}

	@Override
	public Mono<List<GetChatByCategoryDto>> getChatsByCategoryIdBefore(String categoryId, Instant before, int page,
		int size) {
		int skip = page * size;

		return chatRepository.findByCategoryUuidAndCreatedAtBeforeOrderByCreatedAtDesc(categoryId, before)
			.skip(skip)
			.take(size)
			.collectList()
			.flatMap(this::groupByCategory);
	}

	@Override
	public Mono<List<GetChatByCategoryDto>> getChatsByCategoryIdAfter(String categoryId, Instant after, int page,
		int size) {
		int skip = page * size;

		return chatRepository.findByCategoryUuidAndCreatedAtAfterOrderByCreatedAtDesc(categoryId, after)
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

	private boolean isLink(String text) {
		if (text == null)
			return false;

		String lower = text.toLowerCase();

		boolean hasHttpPrefix = lower.startsWith("https://") || lower.startsWith("http://");

		return hasHttpPrefix;
	}

	private Mono<List<GetChatByCategoryDto>> groupByCategory(List<Chat> chats) {
		Map<String, List<Chat>> grouped = chats.stream()
			.collect(Collectors.groupingBy(Chat::getCategoryUuid));

		return Flux.fromIterable(grouped.entrySet())
			.flatMap(entry -> {
				String categoryId = entry.getKey();
				List<Chat> chatList = entry.getValue();

				return categoryRepository.findById(categoryId)
					.flatMap(category ->
						Flux.fromIterable(chatList)
							.flatMap(chat -> {
								Chat.Refs refs = chat.getRefs();

								Mono<Calendar> calendarMono = (refs != null && refs.getCalendarUuid() != null)
									? calendarRepository.findById(refs.getCalendarUuid())
									: Mono.just(new Calendar());

								Mono<Photo> photoMono = (refs != null && refs.getPhotoUuid() != null)
									?
									photoRepository.findById(refs.getPhotoUuid())
									: Mono.just(new Photo());

								Mono<Link> linkMono = (refs != null && refs.getLinkUuid() != null)
									? linkRepository.findById(refs.getLinkUuid())
									: Mono.just(new Link());

								return Mono.zip(calendarMono, photoMono, linkMono)
									.map(tuple -> {
										Calendar calendar = tuple.getT1();
										Photo photo = tuple.getT2();
										Link link = tuple.getT3();

										return GetChatByCategoryDto.ChatItem.builder()
											.data(chat.getData())
											.scheduleName(calendar.getTitle())
											.scheduleStartDate(calendar.getStartDateTime())
											.scheduleEndDate(calendar.getEndDateTime())
											.imgUrl(photo.getUrl())
											.linkUrl(link.getUrl())
											.timestamp(chat.getCreatedAt())
											.build();
									});

							})
							.collectList()
							.map(chatItems -> GetChatByCategoryDto.builder()
								.categoryId(categoryId)
								.categoryName(category.getName())
								.categoryColor(category.getColor())
								.chatItems(chatItems)
								.build())
					);
			})
			.collectList();
	}

	@Override
	public Mono<Chat> getLatestChatByCategoryId(String categoryId) {
		return chatRepository.findFirstByCategoryUuidOrderByCreatedAtDesc(categoryId);
	}

}

