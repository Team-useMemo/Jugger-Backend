package com.usememo.jugger.domain.chat.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.usememo.jugger.domain.calendar.repository.CalendarRepository;
import com.usememo.jugger.domain.chat.dto.PostChatDto;
import com.usememo.jugger.domain.chat.entity.Chat;
import com.usememo.jugger.domain.chat.repository.ChatRepository;
import com.usememo.jugger.domain.link.repository.LinkRepository;
import com.usememo.jugger.global.exception.chat.CategoryNullException;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class ChatServiceImplementationTest {

	private ChatRepository chatRepository;
	private CalendarRepository calendarRepository;
	private LinkRepository linkRepository;
	private ChatService chatService;

	@BeforeEach
	void setUp() {
		chatRepository = mock(ChatRepository.class);
		calendarRepository = mock(CalendarRepository.class);
		linkRepository = mock(LinkRepository.class);
		chatService = new ChatServiceImplementation(calendarRepository, linkRepository, chatRepository);
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

		// save(chat) 호출 시 Mono<Chat> 리턴하게 설정
		when(chatRepository.save(any(Chat.class)))
			.thenReturn(Mono.just(mock(Chat.class)));

		StepVerifier.create(chatService.postChat(dto))
			.verifyComplete(); // Mono<Void> 완료 확인

		ArgumentCaptor<Chat> captor = ArgumentCaptor.forClass(Chat.class);
		verify(chatRepository).save(captor.capture());

		Chat chat = captor.getValue();
		assertThat(chat.getCategoryUuid()).isEqualTo("category-123");
		assertThat(chat.getData()).isEqualTo("테스트 메시지");
		assertThat(chat.getUserUuid()).isEqualTo("123456789a");
		assertThat(chat.getUuid()).isNotNull();
	}

}