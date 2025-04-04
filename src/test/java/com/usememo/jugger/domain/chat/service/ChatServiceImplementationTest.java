package com.usememo.jugger.domain.chat.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import com.usememo.jugger.domain.calendar.repository.CalendarRepository;
import com.usememo.jugger.domain.category.entity.Category;
import com.usememo.jugger.domain.category.repository.CategoryRepository;
import com.usememo.jugger.domain.chat.dto.GetChatByCategoryDto;
import com.usememo.jugger.domain.chat.dto.PostChatDto;
import com.usememo.jugger.domain.chat.entity.Chat;
import com.usememo.jugger.domain.chat.repository.ChatRepository;
import com.usememo.jugger.domain.link.repository.LinkRepository;
import com.usememo.jugger.global.exception.chat.CategoryNullException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class ChatServiceImplementationTest {

	@Mock
	private ChatRepository chatRepository;
	@Mock
	private CalendarRepository calendarRepository;
	@Mock
	private LinkRepository linkRepository;
	@Mock
	private CategoryRepository categoryRepository;
	@Mock
	private ChatService chatService;

	@BeforeEach
	void setUp() {
		chatRepository = mock(ChatRepository.class);
		calendarRepository = mock(CalendarRepository.class);
		linkRepository = mock(LinkRepository.class);
		categoryRepository = mock(CategoryRepository.class);
		chatService = new ChatServiceImplementation(calendarRepository, linkRepository, categoryRepository,
			chatRepository);

	}

	@Test
	@DisplayName("채팅 입력 - category id null일떄")
	void postChatCategoryNull() {
		PostChatDto postChatDto = PostChatDto.builder()
			.categoryUuid(null)
			.text("놀고싶다")
			.build();
		StepVerifier.create(chatService.postChat(postChatDto))
			.expectError(CategoryNullException.class)
			.verify();

		verify(chatRepository, never()).save(any());
	}

	@Test
	@DisplayName("정상 입력 시 Chat 저장 후 Mono<Void> 반환")
	void postChatSave() {
		PostChatDto dto = PostChatDto.builder()
			.categoryUuid("category-123")
			.text("테스트 메시지")
			.build();

		when(chatRepository.save(any(Chat.class)))
			.thenReturn(Mono.just(mock(Chat.class)));

		StepVerifier.create(chatService.postChat(dto))
			.verifyComplete();

		ArgumentCaptor<Chat> captor = ArgumentCaptor.forClass(Chat.class);
		verify(chatRepository).save(captor.capture());

		Chat chat = captor.getValue();
		assertThat(chat.getCategoryUuid()).isEqualTo("category-123");
		assertThat(chat.getData()).isEqualTo("테스트 메시지");
		assertThat(chat.getUserUuid()).isEqualTo("123456789a");
		assertThat(chat.getUuid()).isNotNull();
	}

	@Test
	@DisplayName("입력으로 주어진 이전 시간의 메세지 반환")
	void getChatsBefore() {
		String categoryId = "category-1";
		ZonedDateTime before = ZonedDateTime.now();

		List<Chat> chats = List.of(sampleChat(categoryId, before.minusMinutes(1)));
		Category category = sampleCategory(categoryId, "여행", "#FFAA00");

		when(chatRepository.findByCreatedAtBeforeOrderByCreatedAtDesc(any()))
			.thenReturn(Flux.fromIterable(chats));

		when(categoryRepository.findById(categoryId))
			.thenReturn(Mono.just(category));

		Mono<List<GetChatByCategoryDto>> result = chatService.getChatsBefore(before, 0, 10);

		StepVerifier.create(result)
			.expectNextMatches(list -> {
				return list.size() == 1 &&
					list.get(0).getCategoryId().equals(categoryId) &&
					list.get(0).getCategoryName().equals("여행") &&
					list.get(0).getChatItems().size() == 1;
			})
			.verifyComplete();
	}

	@Test
	void getChatsAfter() {
		String categoryId = "category-1";
		ZonedDateTime before = ZonedDateTime.now();

		List<Chat> chats = List.of(sampleChat(categoryId, before.minusMinutes(1)));
		Category category = sampleCategory(categoryId, "여행", "#FFAA00");

		when(chatRepository.findByCreatedAtBeforeOrderByCreatedAtDesc(any()))
			.thenReturn(Flux.fromIterable(chats));

		when(categoryRepository.findById(categoryId))
			.thenReturn(Mono.just(category));

		Mono<List<GetChatByCategoryDto>> result = chatService.getChatsBefore(before, 0, 10);

		StepVerifier.create(result)
			.expectNextMatches(list -> {
				return list.size() == 1 &&
					list.get(0).getCategoryId().equals(categoryId) &&
					list.get(0).getCategoryName().equals("여행") &&
					list.get(0).getChatItems().size() == 1;
			})
			.verifyComplete();
	}

	private Chat sampleChat(String categoryId, ZonedDateTime time) {
		return Chat.builder()
			.uuid(UUID.randomUUID().toString())
			.userUuid("user-1")
			.categoryUuid(categoryId)
			.data("테스트 채팅")
			.refs(Chat.Refs.builder()
				.calendarUuid("1234a")
				.linkUuid("1234a")
				.photoUuid("1234a").build())
			.build();
	}

	private Category sampleCategory(String id, String name, String color) {
		return Category.builder()
			.uuid(id)
			.name(name)
			.color(color)
			.build();
	}
}