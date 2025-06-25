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
import com.usememo.jugger.domain.category.entity.Category;
import com.usememo.jugger.domain.category.repository.CategoryRepository;
import com.usememo.jugger.domain.chat.dto.GetChatByCategoryDto;
import com.usememo.jugger.domain.chat.dto.GetChatTypeDto;
import com.usememo.jugger.domain.chat.dto.PostChatDto;
import com.usememo.jugger.domain.chat.dto.PostChatTextDto;
import com.usememo.jugger.domain.chat.entity.Chat;
import com.usememo.jugger.domain.chat.entity.ChatType;
import com.usememo.jugger.domain.chat.repository.ChatRepository;
import com.usememo.jugger.domain.link.entity.Link;
import com.usememo.jugger.domain.link.repository.LinkRepository;
import com.usememo.jugger.domain.photo.entity.Photo;
import com.usememo.jugger.domain.photo.repository.PhotoRepository;
import com.usememo.jugger.global.exception.BaseException;
import com.usememo.jugger.global.exception.ErrorCode;
import com.usememo.jugger.global.exception.chat.CategoryNullException;
import com.usememo.jugger.global.security.CustomOAuth2User;

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
	public Mono<Void> processChat(PostChatDto postChatDto, CustomOAuth2User customOAuth2User) {
		// TODO: AI 서버 결과는 실제 배포 시 교체 예정
		GetChatTypeDto getChatTypeDto = GetChatTypeDto.builder()
			.chatType(ChatType.CALENDAR)
			.build();

		return switch (getChatTypeDto.getChatType()) {
			case CALENDAR -> saveCalendar(postChatDto, customOAuth2User);
			case LINK -> saveLink(postChatDto, customOAuth2User);
			default -> Mono.empty();
		};
	}

	@Override
	public Mono<Void> postChat(PostChatDto postChatDto, CustomOAuth2User customOAuth2User) {
		return Mono.justOrEmpty(postChatDto.getCategoryUuid())
			.switchIfEmpty(Mono.error(new CategoryNullException()))
			.flatMap(categoryUuid -> {
				if (isLink(postChatDto.getText())) {
					return saveLinkChat(postChatDto.getText(), categoryUuid, customOAuth2User);
				} else {
					return saveTextChat(postChatDto.getText(), categoryUuid, customOAuth2User);
				}
			});
	}

	@Override
	public Mono<String> postChatWithoutCategory(PostChatTextDto postChatTextDto, CustomOAuth2User customOAuth2User) {
		return createDefaultCategory(customOAuth2User)
			.flatMap(category -> {
				String categoryUuid = category.getUuid();
				Mono<Void> saveMono;
				if (isLink(postChatTextDto.text())) {
					saveMono = saveLinkChat(postChatTextDto.text(), categoryUuid, customOAuth2User);
				} else {
					saveMono = saveTextChat(postChatTextDto.text(), categoryUuid, customOAuth2User);
				}
				return saveMono.thenReturn(categoryUuid);
			});
	}

	/**
	 * 카테고리 없이 채팅 요청 시, 임의의 카테고리를 생성하고 UUID를 리턴.
	 * 생성된 카테고리는 추후 사용자에게 이름과 색상을 선택하게 할 수 있다.
	 */
	private Mono<Category> createDefaultCategory(CustomOAuth2User user) {
		return Mono.defer(() -> {
			Category newCategory = Category.builder()
				.uuid("temp")
				.name("")
				.color("")
				.userUuid(user.getUserId())
				.isPinned(false)
				.build();

			return categoryRepository.save(newCategory);
		});
	}

	private Mono<Void> saveLinkChat(String text, String categoryUuid, CustomOAuth2User customOAuth2User) {
		String chatUuid = UUID.randomUUID().toString();
		String linkUuid = UUID.randomUUID().toString();

		Link link = Link.builder()
			.uuid(linkUuid)
			.userUuid(customOAuth2User.getUserId())
			.categoryUuid(categoryUuid)
			.url(text)
			.build();

		Chat chat = Chat.builder()
			.uuid(chatUuid)
			.userUuid(customOAuth2User.getUserId())
			.categoryUuid(categoryUuid)
			.data(text)
			.refs(Chat.Refs.builder().linkUuid(linkUuid).build())
			.build();

		return linkRepository.save(link)
			.then(chatRepository.save(chat))
			.then(); // Mono<Void>
	}

	private Mono<Void> saveTextChat(String text, String categoryUuid, CustomOAuth2User customOAuth2User) {
		String chatUuid = UUID.randomUUID().toString();

		Chat chat = Chat.builder()
			.uuid(chatUuid)
			.userUuid(customOAuth2User.getUserId())
			.categoryUuid(categoryUuid)
			.data(text)
			.build();

		return chatRepository.save(chat).then();
	}

	@Override
	public Mono<List<GetChatByCategoryDto>> getChatsBefore(Instant before, int page, int size,
		CustomOAuth2User customOAuth2User) {
		int skip = page * size;
		return chatRepository.findByUserUuidAndCreatedAtBeforeOrderByCreatedAtDesc(customOAuth2User.getUserId(), before)
			.skip(skip)
			.take(size)
			.collectList()
			.flatMap(this::groupByCategory);
	}

	@Override
	public Mono<List<GetChatByCategoryDto>> getChatsAfter(Instant after, int page, int size,
		CustomOAuth2User customOAuth2User) {
		int skip = page * size;

		return chatRepository.findByUserUuidAndCreatedAtAfterOrderByCreatedAtDesc(customOAuth2User.getUserId(), after)
			.skip(skip)
			.take(size)
			.collectList()
			.flatMap(this::groupByCategory);
	}

	@Override
	public Mono<List<GetChatByCategoryDto>> getChatsByCategoryIdBefore(CustomOAuth2User customOAuth2User, String categoryId, Instant before, int page,
		int size) {
		int skip = page * size;
		String userId = customOAuth2User.getUserId();

		return chatRepository.findByCategoryUuidAndUserUuidAndCreatedAtBeforeOrderByCreatedAtDesc(userId,categoryId, before)
			.skip(skip)
			.take(size)
			.collectList()
			.flatMap(this::groupByCategory);
	}

	@Override
	public Mono<List<GetChatByCategoryDto>> getChatsByCategoryIdAfter(CustomOAuth2User customOAuth2User,String categoryId, Instant after, int page,
		int size) {
		int skip = page * size;
		String userId = customOAuth2User.getUserId();

		return chatRepository.findByCategoryUuidAndUserUuidAndCreatedAtAfterOrderByCreatedAtDesc(userId,categoryId, after)
			.skip(skip)
			.take(size)
			.collectList()
			.flatMap(this::groupByCategory);
	}

	private Mono<Void> saveCalendar(PostChatDto dto, CustomOAuth2User customOAuth2User) {
		Calendar calendar = Calendar.builder()
			.uuid(UUID.randomUUID().toString())
			.userUuid(customOAuth2User.getUserId())
			.title(dto.getText())
			.startDateTime(Instant.from(LocalDateTime.now()))
			.endDateTime(Instant.from(LocalDateTime.now()))
			.categoryUuid(dto.getCategoryUuid())
			.build();

		return calendarRepository.save(calendar).then();
	}

	private Mono<Void> saveLink(PostChatDto dto, CustomOAuth2User customOAuth2User) {
		Link link = Link.builder()
			.uuid(UUID.randomUUID().toString())
			.userUuid(customOAuth2User.getUserId())
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


										String content;
										String type;
										if (photo.getUuid() != null) {
											content = photo.getUrl();
											type = "PHOTO";
										} else if (calendar.getUuid() != null) {
											content = calendar.getDescription();
											type = "CALENDAR";
										} else if (link.getUuid() != null) {
											content = link.getUrl();
											type = "LINK";
										} else {
											content = chat.getData();
											type = "TEXT";
										}

										return GetChatByCategoryDto.ChatItem.builder()
											.chatId(chat.getId())
											.type(type)
											.content(content)
											.linkUrl(link.getUrl())
											.imgUrl(photo.getUrl())
											.scheduleName(calendar.getTitle())
											.scheduleStartDate(calendar.getStartDateTime())
											.scheduleEndDate(calendar.getEndDateTime())
											.place(calendar.getPlace())
											.alarm(calendar.getAlarm())
											.description(calendar.getDescription())
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
	public Mono<Chat> getLatestChatByCategoryId(CustomOAuth2User customOAuth2User, String categoryId) {
		String userId = customOAuth2User.getUserId();
		return chatRepository.findFirstByCategoryUuidAndUserUuidOrderByCreatedAtDesc(userId,categoryId);
	}

	@Override
	public Mono<Void> deleteAllChats(CustomOAuth2User customOAuth2User){
		String userId = customOAuth2User.getUserId();

		return Mono.when(
			chatRepository.deleteByUserUuid(userId),
			calendarRepository.deleteByUserUuid(userId),
			categoryRepository.deleteByUserUuid(userId),
			photoRepository.deleteByUserUuid(userId),
			linkRepository.deleteByUserUuid(userId)
			).onErrorResume(e->  Mono.error(new BaseException(ErrorCode.DELETE_ERROR)));
	}

	@Override
	public Mono<Void> changeChat(CustomOAuth2User customOAuth2User, String chatId, String text) {
		String userId = customOAuth2User.getUserId();

		return chatRepository.findByUuidAndUserUuid(chatId, userId)
			.switchIfEmpty(Mono.error(new BaseException(ErrorCode.NO_CHAT)))
			.flatMap(chat -> {
				chat.setData(text);
				return chatRepository.save(chat);
			})
			.then();
	}

	@Override
	public Mono<Void> deleteSingleChat(CustomOAuth2User customOAuth2User, String chatId){
		String userId = customOAuth2User.getUserId();

		return chatRepository.deleteByUuidAndUserUuid(chatId,userId)
			.then();
	}

	@Override
	public Mono<Void> changeCategory(CustomOAuth2User customOAuth2User, String chatId, String newCategoryId){
		String userId = customOAuth2User.getUserId();

		return chatRepository.findByUuidAndUserUuid(chatId,userId)
			.switchIfEmpty(Mono.error(new BaseException(ErrorCode.NO_CHAT)))
			.flatMap( chat ->{
					chat.setCategoryUuid(newCategoryId);
					return chatRepository.save(chat);
				}
			).then();
	}

}

